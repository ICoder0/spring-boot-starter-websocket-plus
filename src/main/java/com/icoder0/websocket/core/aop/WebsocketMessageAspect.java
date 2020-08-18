package com.icoder0.websocket.core.aop;


import com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
public interface WebsocketMessageAspect {

    void handleMessage(WebSocketSession session, WebSocketMessage<?> message);

    void afterConnectionClosed(WebSocketSession session, CloseStatus status);

    void afterConnectionEstablished(WebSocketSession session);

    void handleTransportError(WebSocketSession session, Throwable e);
}
