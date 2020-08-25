package com.icoder0.websocket.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bofa1ex
 * @since 2020/8/7
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebsocketOperation {

    /**
     * Implicitly sets a tag for the operations, legacy support (read description).
     * <p>
     * In swagger-core 1.3.X, this was used as the 'path' that is to host the API Declaration of the
     * resource. This is no longer relevant in swagger-core 1.5.X.
     * <p>
     * If {@link #tags()} is <i>not</i> used, this value will be used to set the tag for the operations described by this
     * resource. Otherwise, the value will be ignored.
     * <p>
     * The leading / (if exists) will be removed.
     *
     * @return tag name for operations under this resource, unless {@link #tags()} is defined.
     */
    @AliasFor("tags")
    String[] value() default "";

    /**
     * A list of tags for API documentation control.
     * Tags can be used for logical grouping of operations by resources or any other qualifier.
     * <p>
     * A non-empty value will override the value provided in {@link #value()}.
     *
     * @return a string array of tag values
     * @since 1.5.2-M1
     */
    @AliasFor("value")
    String[] tags() default "";
}
