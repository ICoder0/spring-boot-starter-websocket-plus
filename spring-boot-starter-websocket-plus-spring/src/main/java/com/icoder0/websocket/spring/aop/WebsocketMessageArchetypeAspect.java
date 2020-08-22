package com.icoder0.websocket.spring.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Slf4j
@Aspect
public class WebsocketMessageArchetypeAspect {

    @Autowired
    private WebsocketMessageAspectHandler websocketMessageAspectHandler;

    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleMessage(..))" +
            "&& args(session, message)", argNames = "session, message")
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        websocketMessageAspectHandler.handleMessage(session, message);
    }

    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.afterConnectionEstablished(..))" +
            "&& args(session)", argNames = "session")
    public void afterConnectionEstablished(WebSocketSession session) {
        websocketMessageAspectHandler.afterConnectionEstablished(session);
    }

    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.afterConnectionClosed(..))" +
            "&& args(session, status)", argNames = "session, status")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        websocketMessageAspectHandler.afterConnectionClosed(session, status);
    }

    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleTransportError(..))" +
            "&& args(session,e)", argNames = "session, e")
    public void handleTransportError(WebSocketSession session, Throwable e) {
        websocketMessageAspectHandler.handleTransportError(session, e);
    }

}
