package com.icoder0.websocket.spring.aop;


import com.icoder0.websocket.core.model.WsOutboundBeanSpecification;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
public interface WebsocketMessageAspectHandler {

    void handleInboundMessage(WebSocketSession session, WebSocketMessage<?> message);

    void handleOutboundMessage(WebSocketSession session, WsOutboundBeanSpecification outboundBean);

    void afterConnectionClosed(WebSocketSession session, CloseStatus status);

    void afterConnectionEstablished(WebSocketSession session);

    void handleTransportError(WebSocketSession session, Throwable e);
}
