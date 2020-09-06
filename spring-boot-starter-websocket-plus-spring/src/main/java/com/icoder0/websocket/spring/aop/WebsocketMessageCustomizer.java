package com.icoder0.websocket.spring.aop;

import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/9/6
 */
@FunctionalInterface
public interface WebsocketMessageCustomizer {

    void customize(WebSocketSession session, WebSocketMessage<?> message);
}
