package com.icoder0.websocket.core.annotation;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import javax.validation.Validator;

/**
 * {@code @Configuration} class that registers a {@link WebsocketAnnotationBeanPostProcessor}
 * * bean capable of processing Spring's @{@link WebsocketMapping etc} annotation.
 *
 * @author bofa1ex
 * @see EnableWebsocketPlus
 * @see WebsocketAnnotationBeanPostProcessor
 * @since 2020/8/1
 */
@Configuration
@EnableConfigurationProperties(WebsocketPlusProperties.class)
@AutoConfigureAfter(DelegatingWebSocketConfiguration.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class WebsocketPlusAutoConfiguration {


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer(@Autowired WebsocketPlusProperties websocketPlusProperties) {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setAsyncSendTimeout(websocketPlusProperties.getAsyncSendTimeout());
        container.setMaxSessionIdleTimeout(websocketPlusProperties.getMaxSessionIdleTimeout());
        container.setMaxBinaryMessageBufferSize(websocketPlusProperties.getMaxBinaryMessageBufferSize());
        container.setMaxTextMessageBufferSize(websocketPlusProperties.getMaxTextMessageBufferSize());
        return container;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Order
    public WebsocketAnnotationBeanPostProcessor websocketAnnotationBeanPostProcessor(
            @Autowired WebsocketPlusProperties websocketPlusProperties,
            @Autowired(required = false) HandshakeInterceptor... handlerInterceptors) {
        return new WebsocketAnnotationBeanPostProcessor(websocketPlusProperties, handlerInterceptors);
    }
}
