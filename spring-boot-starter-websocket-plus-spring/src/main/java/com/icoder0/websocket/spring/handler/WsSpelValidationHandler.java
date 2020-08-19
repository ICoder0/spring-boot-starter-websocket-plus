package com.icoder0.websocket.spring.handler;

/**
 * @author bofa1ex
 * @since 2020/8/18
 */
public interface WsSpelValidationHandler {

    void validate(Object inboundBean, String... spelExpressions);
}
