package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.annotation.WebsocketAdvice;
import com.icoder0.websocket.annotation.WebsocketExceptionHandler;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.spring.WebsocketArchetypeHandler;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.handler.model.WsExceptionHandlerMethodMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMethodMetadata;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.util.TypeUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

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

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        final Class<?> beanClazz = AopProxyUtils.ultimateTargetClass(bean);
        Optional.ofNullable(AnnotationUtils.findAnnotation(bean.getClass(), WebsocketMapping.class)).ifPresent(websocketMapping -> {
            /* @Aspect代理类, 会在createBean实例后自动织入cglib/jdk代理. */
            final WebsocketArchetypeHandler archetypeHandler = (WebsocketArchetypeHandler) beanFactory.createBean(WebsocketArchetypeHandler.class, AUTOWIRE_BY_NAME, false);
            final List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList = MethodIntrospector.selectMethods(beanClazz,
                    (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, WebsocketMethodMapping.class)).parallelStream()
                    .filter(method -> _checkMethodParameterValid(method.getParameters()))
                    .map(method -> WsMappingHandlerMethodMetadata.builder()
                            .value(AnnotationUtils.getAnnotation(method, WebsocketMethodMapping.class).value())
                            .bean(bean).method(method).build())
                    .collect(Collectors.toList());
            archetypeHandler.setMappingMethodMetadataList(mappingMethodMetadataList);
            archetypeHandler.setLocation(bean.getClass().getPackage().getName());
            websocketProcessorAttributes.getMappingHandlerMethodMetadataMap().put(websocketMapping.mapping(), WsMappingHandlerMetadata
                    .builder()
                    .archetypeHandler(archetypeHandler)
                    .wsMappingHandlerMethodMetadatas(mappingMethodMetadataList)
                    .build());
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
