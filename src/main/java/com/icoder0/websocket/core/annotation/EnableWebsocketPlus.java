package com.icoder0.websocket.core.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/1
 * @see WebsocketPlusAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(WebsocketPlusAutoConfiguration.class)
@Documented
public @interface EnableWebsocketPlus {
}
