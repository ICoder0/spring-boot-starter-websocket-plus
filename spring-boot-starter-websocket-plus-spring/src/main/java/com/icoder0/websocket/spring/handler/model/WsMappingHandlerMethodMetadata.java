package com.icoder0.websocket.spring.handler.model;

import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author bofa1ex
 * @since 2020/8/17
 */
@Getter
@Builder
public class WsMappingHandlerMethodMetadata {
    /* spel-expression */
    private final String[] value;
    private final Method method;
    private final Object bean;
}
