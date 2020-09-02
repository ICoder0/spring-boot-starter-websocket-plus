package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.annotation.WebsocketRequestParam;
import com.icoder0.websocket.core.exception.WsBusiCode;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.exception.WsExpressionException;
import com.icoder0.websocket.core.exception.WsMappingPrototypeException;
import com.icoder0.websocket.spring.WebsocketArchetypeHandler;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMethodMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMethodParameterMetadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Slf4j
@Getter
public class WebsocketPlusHandlerPostProcessor implements ApplicationContextAware, BeanPostProcessor {

    @Autowired(required = false)
    private List<HandshakeInterceptor> handshakeInterceptors;

    @Autowired
    private WebsocketPlusProperties websocketPlusProperties;

    @Autowired
    private WebsocketProcessorAttributes websocketProcessorAttributes;

    private ConfigurableListableBeanFactory beanFactory;

    private final Map<String, Method> methodExpressions = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        final Class<?> beanClazz = AopProxyUtils.ultimateTargetClass(bean);
        final WebsocketMapping websocketMapping = AnnotationUtils.findAnnotation(bean.getClass(), WebsocketMapping.class);
        final Map<String, WsMappingHandlerMetadata> mappingHandlerMethodMetadataMap = websocketProcessorAttributes.getMappingHandlerMethodMetadataMap();
        if (Objects.isNull(websocketMapping)) {
            return bean;
        }
        final String mapping = websocketMapping.mapping();
        final List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList = MethodIntrospector.selectMethods(beanClazz,
                (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, WebsocketMethodMapping.class)).parallelStream()
                .peek(this::_checkMethodMappingExpressionValid)
                .peek(method -> _checkMethodParameterValid(method.getParameters()))
                .map(method -> WsMappingHandlerMethodMetadata.builder()
                        .value(AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).expr())
                        .parameters(_mapperMethodParameters(method))
                        .outerDecodeClazz(websocketPlusProperties.getOuterDecodeClazz())
                        .spelRootName(websocketPlusProperties.getSpelRootName())
                        .method(method)
                        .bean(bean)
                        .build())
                .collect(Collectors.toList());
        // 如果路由已经存在, 判断是否支持prototype.
        if (mappingHandlerMethodMetadataMap.containsKey(mapping) && !websocketMapping.prototype()) {
            throw new WsMappingPrototypeException(String.format("已注册ws路由 %s", mapping));
        }
        mappingHandlerMethodMetadataMap.compute(mapping, (k, metadata) -> {
            if (Objects.isNull(metadata)) {
                /* @Aspect代理类, 会在createBean实例后自动织入cglib/jdk代理. */
                final WebsocketArchetypeHandler archetypeHandler = beanFactory.createBean(WebsocketArchetypeHandler.class);
                archetypeHandler.setMappingMethodMetadataList(mappingMethodMetadataList);
                archetypeHandler.setLocation(bean.getClass().getPackage().getName());
                return WsMappingHandlerMetadata.builder()
                        .archetypeHandler(archetypeHandler)
                        .wsMappingHandlerMethodMetadatas(mappingMethodMetadataList)
                        .build();
            }
            metadata.getWsMappingHandlerMethodMetadatas().addAll(mappingMethodMetadataList);
            // override old method metadata
            metadata.getArchetypeHandler().setMappingMethodMetadataList(metadata.getWsMappingHandlerMethodMetadatas());
            return metadata;
        });
        return bean;
    }


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = ((GenericApplicationContext) applicationContext).getBeanFactory();
    }


    void _checkMethodParameterValid(Parameter... parameters) {
        if (!parameters[0].isNamePresent()) {
            throw new WsException(WsBusiCode.ILLEGAL_REQUEST_ERROR, "获取不到方法参数的真实参数名, 需要指定compiler参数 -parameters");
        }
    }

    void _checkMethodMappingExpressionValid(Method method) {
        final String[] expressions = AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).value();
        final String key = Arrays.stream(expressions)
                .map(expression -> DigestUtils.md5DigestAsHex(expression.getBytes()))
                .collect(Collectors.joining("&"));
        if (methodExpressions.containsKey(key)) {
            final Method existMethod = methodExpressions.get(key);
            throw new WsExpressionException(String.format("[%s] @WebsocketMethodMapping#expression %s冲突, 已存在方法[%s]", method, Arrays.toString(expressions), existMethod));
        }
        methodExpressions.put(key, method);
    }

    List<WsMappingHandlerMethodParameterMetadata> _mapperMethodParameters(Method method) {
        List<WsMappingHandlerMethodParameterMetadata> parameterMetadataList = new LinkedList<>();
        for (final Parameter parameter : method.getParameters()) {
            final Class<?> parameterType = parameter.getType();
            String parameterName = parameter.getName();
            String parameterDefaultValue = null;
            boolean parameterRequired = false;
            boolean needValidated = false;
            // check parameter whether need nest-validate.
            for (Annotation annotation : parameter.getAnnotations()) {
                final WebsocketRequestParam websocketRequestParam = AnnotationUtils.getAnnotation(annotation, WebsocketRequestParam.class);
                if (Objects.nonNull(websocketRequestParam)) {
                    parameterName = websocketRequestParam.name();
                    parameterDefaultValue = websocketRequestParam.defaultValue();
                    parameterRequired = websocketRequestParam.required();
                }
                if (AnnotatedElementUtils.hasAnnotation(parameter, Validated.class)) {
                    needValidated = true;
                }
            }
            parameterMetadataList.add(WsMappingHandlerMethodParameterMetadata.builder()
                    .innerDecodeParamKeyName(websocketPlusProperties.getInnerDecodeParamKeyName())
                    .outerDecodeClazz(websocketPlusProperties.getOuterDecodeClazz())
                    .type(parameterType)
                    .method(method)
                    .require(parameterRequired)
                    .defaultValue(parameterDefaultValue)
                    .name(parameterName)
                    .validated(needValidated)
                    .build()
            );
        }
        return parameterMetadataList;
    }

}
