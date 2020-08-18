package com.icoder0.websocket.core.annotation;

import com.icoder0.websocket.core.handler.model.WsExceptionHandlerMethodMetadata;
import com.icoder0.websocket.core.handler.model.WsMappingHandlerMetadata;
import com.icoder0.websocket.core.handler.model.WsMappingHandlerMethodMetadata;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.util.TypeUtils;
import org.springframework.web.socket.BinaryMessage;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Slf4j
public class WebsocketPlusBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor {

    @Autowired
    private WebsocketPlusMetadataProperties websocketPlusMetadataProperties;

    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        final Class<?> beanClazz = AopProxyUtils.ultimateTargetClass(bean);

        Optional.ofNullable(AnnotationUtils.findAnnotation(bean.getClass(), WebsocketMapping.class)).ifPresent(websocketMapping -> {
            /* @Aspect代理类, 会在createBean实例后自动织入cglib/jdk代理. */
            final WebsocketArchetypeHandler archetypeHandler = beanFactory.createBean(WebsocketArchetypeHandler.class);
            final List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList = MethodIntrospector.selectMethods(beanClazz,
                    (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, WebsocketMethodMapping.class)).parallelStream()
                    .filter(method -> _checkMethodParameterValid(method.getParameters()))
                    .map(method -> WsMappingHandlerMethodMetadata.builder()
                            .value(AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).value())
                            .bean(bean).method(method).build()).collect(Collectors.toList());
            archetypeHandler.setMappingMethodMetadataList(mappingMethodMetadataList);
            websocketPlusMetadataProperties.getMappingHandlerMethodMetadataMap().put(websocketMapping.mapping(), WsMappingHandlerMetadata
                    .builder()
                    .archetypeHandler(archetypeHandler)
                    .wsMappingHandlerMethodMetadatas(mappingMethodMetadataList)
                    .build());
        });

        Optional.ofNullable(AnnotationUtils.findAnnotation(bean.getClass(), WebsocketAdvice.class)).ifPresent(websocketAdvice -> {
            final List<WsExceptionHandlerMethodMetadata> wsExceptionHandlerMethodMetadataList = MethodIntrospector
                    .selectMethods(beanClazz, (ReflectionUtils.MethodFilter) method ->
                            Objects.nonNull(AnnotationUtils.findAnnotation(method, WebsocketExceptionHandler.class))
                    ).parallelStream().flatMap(method -> {
                        final WebsocketExceptionHandler websocketExceptionHandler = AnnotationUtils.getAnnotation(method, WebsocketExceptionHandler.class);
                        return Arrays.stream(websocketExceptionHandler.value()).parallel()
                                .map(exception -> WsExceptionHandlerMethodMetadata.builder()
                                        .value(exception)
                                        .bean(bean)
                                        .method(method)
                                        .build()
                                );
                    }).collect(Collectors.toList());
            websocketPlusMetadataProperties.getExceptionMethodMetadataMap().put(websocketAdvice.basePackages(), wsExceptionHandlerMethodMetadataList);
        });
        return bean;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = ((GenericApplicationContext) applicationContext).getBeanFactory();
    }


    boolean _checkMethodParameterValid(Parameter... parameters) {
        for (Parameter parameter : parameters) {
            final Class<?> parameterType = parameter.getType();
            if (parameterType.isPrimitive() && TypeUtils.isAssignable(CharSequence.class, parameterType)) {
                log.warn("暂不支持{}入参为基本类型或者CharSequence类型, 请填写实体类用于json解析..", parameterType);
                return false;
            }
            if (TypeUtils.isAssignable(BinaryMessage.class, parameterType)) {
                log.warn("暂时不支持BinaryMessage作为入参类型, 请使用TextMessage或者实体类用于json解析..");
                return false;
            }
        }
        return true;
    }
}
