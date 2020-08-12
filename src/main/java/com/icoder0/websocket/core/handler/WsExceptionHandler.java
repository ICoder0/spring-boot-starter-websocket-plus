package com.icoder0.websocket.core.handler;

import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/7/31
 */
public interface WsExceptionHandler {

    void handleException(WebSocketSession session, Throwable t);
}
