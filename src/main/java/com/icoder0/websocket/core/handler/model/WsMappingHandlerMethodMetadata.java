package com.icoder0.websocket.core.handler.model;

import com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;

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
