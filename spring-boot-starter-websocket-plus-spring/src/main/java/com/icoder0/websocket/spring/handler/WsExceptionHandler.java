package com.icoder0.websocket.spring.handler;

import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/7/31
 */
public interface WsExceptionHandler {

    void handleException(WebSocketSession session, Throwable t);
}
