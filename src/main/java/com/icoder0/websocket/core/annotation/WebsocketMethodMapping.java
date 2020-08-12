package com.icoder0.websocket.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebsocketMethodMapping {

    @AliasFor("expr")
    String[] value() default {"true"};

    @AliasFor("value")
    String[] expr() default {"true"};
}
