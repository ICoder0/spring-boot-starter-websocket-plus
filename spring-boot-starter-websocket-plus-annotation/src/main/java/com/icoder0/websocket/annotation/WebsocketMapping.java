package com.icoder0.websocket.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/7/31
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface WebsocketMapping {

    @AliasFor("mapping")
    String[] value() default "";

    @AliasFor("value")
    String[] mapping() default "";

    boolean prototype() default false;
}
