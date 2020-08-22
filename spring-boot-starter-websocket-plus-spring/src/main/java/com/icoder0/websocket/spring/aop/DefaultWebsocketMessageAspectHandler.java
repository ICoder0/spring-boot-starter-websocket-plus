package com.icoder0.websocket.spring.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
@Slf4j
public class DefaultWebsocketMessageAspectHandler implements WebsocketMessageAspectHandler {

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress() + session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        }
    }
}
