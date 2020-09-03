package com.icoder0.websocket.spring.model;

import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author bofa1ex
 * @since 2020/8/17
 */
@Getter
@Builder
public class WsExceptionHandlerMethodMetadata {
    private final Class<? extends Throwable> value;
    private final Integer priority;
    private final Method method;
    private final Object bean;
}
