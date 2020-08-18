package com.icoder0.websocket.core.annotation;

import org.springframework.cglib.SpringCglibInfo;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

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

    String name() default "";

    @AliasFor("mapping")
    String[] value() default {"true"};

    @AliasFor("value")
    String[] mapping() default {"true"};
}
