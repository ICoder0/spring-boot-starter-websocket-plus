package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.annotation.EnableWebsocketPlus;
import com.icoder0.websocket.spring.aop.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * {@code @Configuration} class that registers a bean capable of processing Spring's annotation like above.
 * {@code WebsocketMapping}
 * {@code WebsocketMethodMapping}
 *
 * @author bofa1ex
 * @see EnableWebsocketPlus
 * @see WebsocketMessageCustomizer 用于自定义行为, 处理上行数据.
 * @see WebsocketMessageAspectHandler 用于自定义代理行为, 处理上行数据, 下行数据, 建立连接, 断开连接, 传输出错.
 * @see DefaultWebsocketMessageAspectHandler 默认实现各行为的日志打印, 上行数据规范的校验, 下行数据的自动下发, 建议用户复写建立连接和断开连接的缓存行为.
 * @since 2020/8/1
 */
@Configuration
@ComponentScan("com.icoder0.websocket.spring.exception")
@EnableAspectJAutoProxy
@EnableConfigurationProperties(WebsocketPlusProperties.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class WebsocketPlusAutoConfiguration {

    /**
     * 用户可自定义实现WebsocketMessageAspectHandler覆盖默认实现.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(WebsocketMessageAspectHandler.class)
    public WebsocketMessageAspectHandler websocketMessageAspectHandler() {
        return new DefaultWebsocketMessageAspectHandler();
    }

    /**
     * WebsocketMessageCustomizer的注册表, 提供对上行数据处理行为的批处理.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketMessageCustomizerRegistry websocketMessageCustomizerRegistry(){
        return new WebsocketMessageCustomizerRegistry();
    }

    /**
     * 骨架Handler的核心Aspect, 必须在WebsocketPlusBeanFactoryPostProcessor前注入到容器.
     * 否则在#createBean时, 无法对注入的bean对象cglib代理.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketMessageArchetypeAspect websocketMessageAspect() {
        return new WebsocketMessageArchetypeAspect();
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setAsyncSendTimeout(WebsocketPlusProperties.asyncSendTimeout);
        container.setMaxSessionIdleTimeout(WebsocketPlusProperties.maxSessionIdleTimeout);
        container.setMaxBinaryMessageBufferSize(WebsocketPlusProperties.maxBinaryMessageBufferSize);
        container.setMaxTextMessageBufferSize(WebsocketPlusProperties.maxTextMessageBufferSize);
        return container;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketProcessorAttributes websocketProcessorAttributes() {
        return new WebsocketProcessorAttributes();
    }

    /**
     * 扫描骨架handler必要参数.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketPlusHandlerPostProcessor websocketPlusHandlerPostProcessor() {
        return new WebsocketPlusHandlerPostProcessor();
    }

    /**
     * 扫描全局异常处理必要参数.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public WebsocketPlusExceptionPostProcessor websocketPlusExceptionPostProcessor() {
        return new WebsocketPlusExceptionPostProcessor();
    }
}
