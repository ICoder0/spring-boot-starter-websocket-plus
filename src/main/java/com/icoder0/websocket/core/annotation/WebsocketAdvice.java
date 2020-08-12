package com.icoder0.websocket.core.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
@Component
public @interface WebsocketAdvice {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

}
