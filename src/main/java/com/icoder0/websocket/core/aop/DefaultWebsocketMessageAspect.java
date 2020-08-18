package com.icoder0.websocket.core.aop;


import com.alibaba.fastjson.JSON;
import com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler;
import com.icoder0.websocket.core.annotation.WebsocketPlusMetadataProperties;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.core.handler.model.WsMappingHandlerMethodMetadata;
import com.icoder0.websocket.core.utils.AESUtils;
import com.icoder0.websocket.core.utils.ByteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import javax.websocket.Session;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Aspect
@Slf4j
public class DefaultWebsocketMessageAspect implements WebsocketMessageAspect {

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.handleMessage(..))" +
            "&& args(session, message)", argNames = "session, message")
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress() + session.getId(), message.getPayload());
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.handleMessage(..))" +
            "&& args(session)", argNames = "session")
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.afterConnectionClosed(..))" +
            "&& args(session, status)", argNames = "session, status")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
        }
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.handleTransportError(..))" +
            "&& args(session,e)", argNames = "session, e")
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        }
    }

}
