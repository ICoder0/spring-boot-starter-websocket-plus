package com.icoder0.websocket.core.annotation;

import com.icoder0.websocket.core.aop.DefaultWebsocketMessageAspect;
import com.icoder0.websocket.core.aop.WebsocketMessageAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * {@code @Configuration} class that registers a bean capable of processing Spring's @{@link WebsocketMapping etc} annotation.
 *
 * @author bofa1ex
 * @see EnableWebsocketPlus
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 * @since 2020/8/1
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(WebsocketPlusProperties.class)
@AutoConfigureAfter(WebsocketConfigurationSupportEx.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class WebsocketPlusAutoConfiguration {

    /**
     * @code @Aspect 注解修饰的bean对象必须在WebsocketPlusBeanFactoryPostProcessor前注入到容器, 否则在#createBean时, 无法对注入的bean对象
     * 实现织入cglib代理.
     */
    @Bean
    @ConditionalOnMissingBean(WebsocketMessageAspect.class)
    public DefaultWebsocketMessageAspect websocketMessageAspect() {
        return new DefaultWebsocketMessageAspect();
    }

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
    public WebsocketPlusBeanPostProcessor websocketPlusBeanFactoryPostProcessor() {
        return new WebsocketPlusBeanPostProcessor();
    }
}
