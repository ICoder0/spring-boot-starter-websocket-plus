package com.icoder0.websocket.spring.aop;

import com.alibaba.fastjson.JSON;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
@Slf4j
public class DefaultWebsocketMessageAspectHandler implements WebsocketMessageAspectHandler {

    static String sessionAddress(WebSocketSession session){
        return session.getRemoteAddress() + "@" + session.getId();
    }

    @Override
    public void handleInboundMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", sessionAddress(session), message.getPayload());
    }

    @Override
    public void handleOutboundMessage(WebSocketSession session, Object outboundBean) {
        if (Objects.isNull(outboundBean)) {
            log.warn("{} OUTBOUND IS NULL", sessionAddress(session));
            return;
        }
        log.info("{} OUTBOUND {}", sessionAddress(session), JSON.toJSONString(outboundBean));
        WebsocketMessageEmitter.emit(outboundBean, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("{} 断开连接", sessionAddress(session));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("{} 建立连接", sessionAddress(session));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("{} 传输出错", sessionAddress(session), e);
        }
    }
}
