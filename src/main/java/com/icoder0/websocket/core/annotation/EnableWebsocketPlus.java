package com.icoder0.websocket.core.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/1
 * @see WebsocketPlusAutoConfiguration
 * @see WebsocketAnnotationBeanPostProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(WebsocketPlusAutoConfiguration.class)
@EnableWebSocket
@Documented
public @interface EnableWebsocketPlus {
}
