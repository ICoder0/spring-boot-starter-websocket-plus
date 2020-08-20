package com.icoder0.websocket.example;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.core.model.WsOutboundBean;
import com.icoder0.websocket.core.utils.AESUtils;
import com.icoder0.websocket.core.utils.Assert;
import com.icoder0.websocket.core.utils.ByteUtils;
import com.icoder0.websocket.spring.aop.WebsocketMessageAspect;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author bofa1ex
 * @since 2020/8/18
 */
@Aspect
@Component
@Slf4j
public class WebsocketMessageAspectEx implements WebsocketMessageAspect {

    String USER_ID_ATTRIBUTE_KEY = "USER_ID";

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleMessage(..)) && args(session, message)", argNames = "session, message")
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress() + session.getId(), message.getPayload());
    }
//
//    @Override
//    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.afterConnectionEstablished(..)) && args(session)", argNames = "session")
//    public void afterConnectionEstablished(WebSocketSession session) {
//        Assert.checkCondition(session instanceof StandardWebSocketSession, () -> WebsocketMessageEmitter.emit(WsOutboundBean.builder()
//                .result(ImmutableMap.of(
//                        "code", "400",
//                        "message", "未获取到NativeSession"
//                ))
//                .build(), session)
//        );
//        log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
//        final Session nativeSession = ((StandardWebSocketSession) session).getNativeSession();
//        final Map<String, List<String>> requestParameterMap = nativeSession.getRequestParameterMap();
//        final List<String> tokens = requestParameterMap.getOrDefault("token", new ArrayList<>());
//        Assert.checkXorCondition(tokens.isEmpty(), () -> WebsocketMessageEmitter.emit(WsOutboundBean.builder()
//                .result(ImmutableMap.of(
//                        "code", "400",
//                        "message", "未找到token, 请检查ws#url"
//                ))
//                .build(), session)
//        );
//        /* aes解密, 获取加密数据域和对称密钥 */
//        final String encryptToken = tokens.get(0);
//        final StringTokenizer stringTokenizer = new StringTokenizer(encryptToken, ".");
//        Assert.checkCondition(stringTokenizer.countTokens() == 2, () -> WebsocketMessageEmitter.emit(WsOutboundBean.builder()
//                .result(ImmutableMap.of(
//                        "code", "400",
//                        "message", "请检查token规范"
//                ))
//                .build(), session)
//        );
//        final String encryptHex = stringTokenizer.nextToken();
//        final String encryptKeyHex = stringTokenizer.nextToken();
//        try {
//            byte[] decryptBytes = AESUtils.decrypt_cbc(ByteUtils.hex2Bytes(encryptHex), ByteUtils.hex2Bytes(encryptKeyHex), true);
//            final Long userId = Long.valueOf(StringUtils.toEncodedString(decryptBytes, Charsets.UTF_8));
//            log.info("#DECODE TOKEN userId {}", userId);
//        } catch (RuntimeException e) {
//            WebsocketMessageEmitter.emit(WsOutboundBean.builder()
//                    .result(ImmutableMap.of(
//                            "code", "400",
//                            "message", "请检查token数据, 解析失败"
//                    ))
//                    .build(), session
//            );
//        }
//    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.afterConnectionClosed(..)) && args(session, status)", argNames = "session, status")
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
        }
        final Object userId = session.getAttributes().get(USER_ID_ATTRIBUTE_KEY);
        log.warn("[{}]缓存已清理 [{}]", session.getRemoteAddress() + "@" + session.getId(), userId);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

    }

    @Override
    @Before(value = "execution(* com.icoder0.websocket.spring.WebsocketArchetypeHandler.handleTransportError(..)) && args(session,e)", argNames = "session, e")
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        }
    }

}
