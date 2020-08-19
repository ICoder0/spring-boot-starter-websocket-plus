package com.icoder0.websocket.spring.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Aspect
@Slf4j
public class DefaultWebsocketMessageAspect implements WebsocketMessageAspect {

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleMessage(..))" +
            "&& args(session, message)", argNames = "session, message")
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress() + session.getId(), message.getPayload());
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleMessage(..))" +
            "&& args(session)", argNames = "session")
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.afterConnectionClosed(..))" +
            "&& args(session, status)", argNames = "session, status")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleTransportError(..))" +
            "&& args(session,e)", argNames = "session, e")
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        }
    }

}
