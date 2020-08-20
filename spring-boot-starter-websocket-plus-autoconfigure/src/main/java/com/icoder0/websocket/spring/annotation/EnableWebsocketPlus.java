package com.icoder0.websocket.spring.annotation;

import com.icoder0.websocket.spring.configuration.WebsocketPlusAutoConfiguration;
import com.icoder0.websocket.spring.configuration.WebsocketRegistrySupport;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @see WebsocketPlusAutoConfiguration
 * @since 2020/8/1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({WebsocketPlusAutoConfiguration.class, WebsocketRegistrySupport.class})
@Documented
public @interface EnableWebsocketPlus {
}
