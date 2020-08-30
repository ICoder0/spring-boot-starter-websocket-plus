package com.icoder0.websocket.spring.handler.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsRequestParamException;
import com.icoder0.websocket.core.utils.Assert;
import lombok.Builder;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

/**
 * @author bofa1ex
 * @since 2020/8/28
 */
@Builder
public class WsMappingHandlerMethodParameterMetadata {

    private final Class<?> outerDecodeClazz;
    private final String innerDecodeParamKeyName;

    private final Method method;
    private final String parameterName;
    private final String parameterDefaultValue;

    private final Class<?> parameterType;
    private final boolean validated;

    public Object extractArg(WebSocketSession session, WebSocketMessage<?> webSocketMessage) {
        if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameterType)) {
            return session;
        }
        // Text message
        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, webSocketMessage.getClass())) {
            final TextMessage textMessage = TypeUtils.cast(webSocketMessage, TextMessage.class, ParserConfig.getGlobalInstance());
            if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, parameterType)) {
                return webSocketMessage;
            }
            // 基本类型
            if (parameterType.isPrimitive() || org.springframework.util.TypeUtils.isAssignable(CharSequence.class, parameterType)) {
                final JSONObject payload = Optional.ofNullable(JSON.parseObject(textMessage.getPayload()))
                        .orElseThrow(() -> new WsRequestParamException("检查payload规范"));
                final JSONObject payloadParams = Optional.ofNullable(payload.getJSONObject(innerDecodeParamKeyName))
                        .orElseThrow(() -> new WsRequestParamException("检查payload#params规范"));
                Assert.checkCondition(payloadParams.containsKey(parameterName),
                        () -> new WsRequestParamException(method.toString())
                );
                return payloadParams.getObject(parameterName, parameterType);
            }
            if (org.springframework.util.TypeUtils.isAssignable(Map.class, parameterType)) {
                return JSON.parseObject(textMessage.getPayload(), Map.class);
            }
            if (org.springframework.util.TypeUtils.isAssignable(outerDecodeClazz, parameterType)) {
                return JSON.parseObject(textMessage.getPayload(), outerDecodeClazz);
            }
            final JSONObject payload = Optional.ofNullable(JSON.parseObject(textMessage.getPayload()))
                    .orElseThrow(() -> new WsRequestParamException("检查payload规范"));
            return payload.getObject(innerDecodeParamKeyName, parameterType);
        }


        // binary message
        if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, parameterType)) {
            return webSocketMessage;
        }
        if (org.springframework.util.TypeUtils.isAssignable(ByteBuffer.class, parameterType)) {
            if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, parameterType)) {
                return webSocketMessage.getPayload();
            }
        }
        if (org.springframework.util.TypeUtils.isAssignable(byte[].class, parameterType)) {
            if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, parameterType)) {
                final BinaryMessage binaryMessage = TypeUtils.cast(webSocketMessage, BinaryMessage.class, ParserConfig.getGlobalInstance());
                return binaryMessage.getPayload().array();
            }
        }

        return null;
    }
}
