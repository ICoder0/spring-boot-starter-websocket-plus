package com.icoder0.websocket.examples;

import com.google.common.base.Charsets;
import com.icoder0.websocket.core.aop.WebsocketMessageAspect;
import com.icoder0.websocket.core.utils.AESUtils;
import com.icoder0.websocket.core.utils.ByteUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/8/18
 */
@Aspect
@Component
@Slf4j
public class WebsocketMessageAspectEx implements WebsocketMessageAspect {

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.handleMessage(..)) && args(session, message)", argNames = "session, message")
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress() + session.getId(), message.getPayload());
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.afterConnectionEstablished(..)) && args(session)", argNames = "session")
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
        }
        Assert.isAssignable(session.getClass(), StandardWebSocketSession.class, "未获取到NativeSession");
        final Session nativeSession = ((StandardWebSocketSession) session).getNativeSession();
        final Map<String, List<String>> requestParameterMap = nativeSession.getRequestParameterMap();
        final String encryptToken = requestParameterMap.get("token").parallelStream().findFirst()
                .orElseThrow(() -> new RuntimeException("未获取到token"));
        /* aes解密, 获取加密数据域和对称密钥 */
        final String encryptHex = encryptToken.substring(0, encryptToken.indexOf("."));
        final String encryptKeyHex = encryptToken.substring(encryptToken.indexOf(".") + 1);
        byte[] decryptBytes = AESUtils.decrypt_cbc(ByteUtils.hex2Bytes(encryptHex), ByteUtils.hex2Bytes(encryptKeyHex), true);
        /* 解密获取userId */
        final Long userId = Long.valueOf(StringUtils.toEncodedString(decryptBytes, Charsets.UTF_8));
        System.out.println(userId);
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.afterConnectionClosed(..)) && args(session, status)", argNames = "session, status")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
        }
        final Object userId = session.getAttributes().get(SessionConstants.USER_ID_ATTRIBUTE_KEY);
        log.warn("[{}]缓存已清理 [{}]", session.getRemoteAddress() + "@" + session.getId(), userId);
    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler.handleTransportError(..)) && args(session,e)", argNames = "session, e")
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        }
    }
}
