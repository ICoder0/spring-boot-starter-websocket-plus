package com.icoder0.websocket.spring.configuration;


import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.annotation.EnableWebsocketPlus;
import com.icoder0.websocket.spring.aop.DefaultWebsocketMessageAspect;
import com.icoder0.websocket.spring.aop.WebsocketMessageAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
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
@ComponentScan("com.icoder0.websocket.spring.exception")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(WebsocketPlusProperties.class)
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
    public WebsocketProcessorAttributes websocketProcessorAttributes() {
        return new WebsocketProcessorAttributes();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketPlusHandlerPostProcessor websocketPlusHandlerPostProcessor() {
        return new WebsocketPlusHandlerPostProcessor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketPlusExceptionPostProcessor websocketPlusExceptionPostProcessor() {
        return new WebsocketPlusExceptionPostProcessor();
    }
}
