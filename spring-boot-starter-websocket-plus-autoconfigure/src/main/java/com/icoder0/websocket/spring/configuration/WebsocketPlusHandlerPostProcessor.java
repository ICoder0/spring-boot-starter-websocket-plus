package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.annotation.WebsocketHeader;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.annotation.WebsocketPayload;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.exception.WsExceptionTemplate;
import com.icoder0.websocket.core.model.WsBusiCode;
import com.icoder0.websocket.spring.WebsocketArchetypeHandler;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.model.WsMappingHandlerMetadata;
import com.icoder0.websocket.spring.model.WsMappingHandlerMethodMetadata;
import com.icoder0.websocket.spring.model.WsMappingHandlerMethodParameterMetadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
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
 * @see WsExceptionTemplate 异常输出模板
 * @since 2020/8/14
 */
@Slf4j
@Getter
public class WebsocketPlusHandlerPostProcessor implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

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
        final String[] mappings = websocketMapping.mapping();
        final List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList = MethodIntrospector.selectMethods(beanClazz,
                (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, WebsocketMethodMapping.class)).stream()
                // 校验methodMapping#expression是否存在冲突
                .peek(this::_checkMethodMappingExpressionValid)
                // 校验方法入参名compile是否符合要求
                .peek(method -> _checkMethodParameterValid(method.getParameters()))
                .map(method -> WsMappingHandlerMethodMetadata.builder()
                        .expressions(AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).expr())
                        .inboundBeanClazz(WebsocketPlusProperties.inboundBeanClazz)
                        .spelVariableName(WebsocketPlusProperties.spelVariableName)
                        .parameters(_mapperMethodParameters(method))
                        .method(method)
                        .bean(bean)
                        .build())
                .collect(Collectors.toList());
        // 如果路由已经存在, 判断是否支持prototype.
        for (String mapping : mappings) {
            if (mappingHandlerMethodMetadataMap.containsKey(mapping) && !websocketMapping.prototype()) {
                final WsMappingHandlerMetadata wsMappingHandlerMetadata = mappingHandlerMethodMetadataMap.get(mapping);
                final String existBeanName = wsMappingHandlerMetadata.getBeanName();
                throw new WsException(WsBusiCode.INTERNAL_ERROR, String.format(
                        WsExceptionTemplate.MAPPING_ALREADY_REGISTER, existBeanName, mapping, beanClazz.getSimpleName())
                );
            }
            mappingHandlerMethodMetadataMap.compute(mapping, (k, metadata) -> {
                if (Objects.isNull(metadata)) {
                    /*
                     * createBean自动织入cglib代理.
                     * @see com.icoder0.websocket.spring.aop.WebsocketMessageArchetypeAspect
                     */
                    final WebsocketArchetypeHandler archetypeHandler = beanFactory.createBean(WebsocketArchetypeHandler.class);
                    archetypeHandler.setMappingMethodMetadataList(mappingMethodMetadataList);
                    archetypeHandler.setWebsocketPlusProperties(websocketPlusProperties);
                    archetypeHandler.setLocation(bean.getClass().getPackage().getName());
                    return WsMappingHandlerMetadata.builder()
                            .beanName(beanClazz.getSimpleName())
                            .archetypeHandler(archetypeHandler)
                            .wsMappingHandlerMethodMetadatas(mappingMethodMetadataList)
                            .build();
                }
                metadata.getWsMappingHandlerMethodMetadatas().addAll(mappingMethodMetadataList);
                // override old method metadata
                metadata.getArchetypeHandler().setMappingMethodMetadataList(metadata.getWsMappingHandlerMethodMetadatas());
                return metadata;
            });
        }
        return bean;
    }


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = ((GenericApplicationContext) applicationContext).getBeanFactory();
    }


    void _checkMethodParameterValid(Parameter... parameters) {
        if (!parameters[0].isNamePresent()) {
            throw new WsException(WsBusiCode.INTERNAL_ERROR, WsExceptionTemplate.METHOD_PARAMETER_NAME_PRESENT);
        }
    }

    void _checkMethodMappingExpressionValid(Method method) {
        final String[] expressions = AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).expr();
        final String key = Arrays.stream(expressions).map(expression -> DigestUtils.md5DigestAsHex(expression.getBytes())).collect(Collectors.joining("&"));
        if (methodExpressions.containsKey(key)) {
            final Method existMethod = methodExpressions.get(key);
            throw new WsException(WsBusiCode.INTERNAL_ERROR, String.format(
                    WsExceptionTemplate.METHOD_MAPPING_EXPRESSION_CONFLICT, existMethod, Arrays.toString(expressions), method)
            );
        }
        methodExpressions.put(key, method);
    }

    List<WsMappingHandlerMethodParameterMetadata> _mapperMethodParameters(Method method) {
        final List<WsMappingHandlerMethodParameterMetadata> parameterMetadataList = new LinkedList<>();
        for (final Parameter parameter : method.getParameters()) {
            final Class<?> parameterType = parameter.getType();
            String parameterName = parameter.getName();
            String parameterDefaultValue = null;
            boolean parameterRequired = false;
            boolean needValidated = false;
            boolean isHeader = false;
            boolean isNormal = true;
            /*
             * 1. check parameter whether need nest-validate.
             * 2. process parameter which modify by WebsocketPayload.
             * 3. process parameter which modify by WebsocketHeader.
             */
            for (Annotation annotation : parameter.getAnnotations()) {
                final WebsocketPayload websocketPayload = AnnotationUtils.getAnnotation(annotation, WebsocketPayload.class);
                final WebsocketHeader websocketHeader = AnnotationUtils.getAnnotation(annotation, WebsocketHeader.class);

                if (AnnotatedElementUtils.hasAnnotation(parameter, Validated.class)) {
                    needValidated = true;
                }
                if (Objects.nonNull(websocketPayload)) {
                    isNormal = false;
                    parameterName = websocketPayload.name();
                    parameterDefaultValue = websocketPayload.defaultValue();
                    parameterRequired = websocketPayload.required();
                }
                if (Objects.nonNull(websocketHeader)) {
                    isNormal = false;
                    isHeader = true;
                    parameterName = websocketHeader.isSequence() ? WebsocketPlusProperties.payloadSequenceDecodeName : null;
                    parameterName = Objects.isNull(parameterName) ?
                            websocketHeader.isFunctionCode() ? WebsocketPlusProperties.payloadFunctionCodeDecodeName : null : parameterName;
                }
            }
            parameterMetadataList.add(WsMappingHandlerMethodParameterMetadata.builder()
                    .inboundBeanClazz(WebsocketPlusProperties.inboundBeanClazz)
                    .payloadParamsDecodeName(WebsocketPlusProperties.payloadParamsDecodeName)
                    .type(parameterType)
                    .method(method)
                    .isHeader(isHeader)
                    .isNormal(isNormal)
                    .require(parameterRequired)
                    .defaultValue(parameterDefaultValue)
                    .name(parameterName)
                    .validated(needValidated)
                    .build()
            );
        }
        return parameterMetadataList;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        methodExpressions.clear();
    }
}
