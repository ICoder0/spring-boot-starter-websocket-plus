package com.icoder0.websocket.core.annotation;

import java.lang.annotation.*;

/**
 * @author bofa1ex
 * @since 2020/8/9
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebsocketExceptionHandler {

    /**
     * Exceptions handled by the annotated method. If empty, will default to any
     * exceptions listed in the method argument list.
     */
    Class<? extends Throwable>[] value() default {};
}
