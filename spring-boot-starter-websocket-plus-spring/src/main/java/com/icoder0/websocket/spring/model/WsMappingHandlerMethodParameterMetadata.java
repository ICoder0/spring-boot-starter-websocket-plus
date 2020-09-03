package com.icoder0.websocket.spring.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsBusiCode;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.exception.WsExceptionTemplate;
import com.icoder0.websocket.core.exception.WsRequestParamException;
import com.icoder0.websocket.core.utils.Assert;
import io.netty.buffer.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.*;

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

    private final Class<?> payloadDecodeClazz;
    private final String payloadParamsDecodeName;

    private final String payloadSpecification;
    private final String payloadParamSpecification;

    private final Method method;
    private final String name;
    private final String defaultValue;
    private final boolean require;

    private final Class<?> type;
    @Getter
    private final boolean validated;

    public Object extractArg(WebSocketSession session, WebSocketMessage<?> webSocketMessage) {
        if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, type)) {
            return session;
        }
        // Text message
        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, webSocketMessage.getClass())) {
            final TextMessage textMessage = TypeUtils.cast(webSocketMessage, TextMessage.class, ParserConfig.getGlobalInstance());
            if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, type)) {
                return webSocketMessage;
            }
            // primitive type
            if (ClassUtils.isPrimitiveOrWrapper(type) || org.springframework.util.TypeUtils.isAssignable(CharSequence.class, type)) {
                final JSONObject payload = Optional.ofNullable(JSON.parseObject(textMessage.getPayload()))
                        .orElseThrow(() -> new WsRequestParamException(String.format(
                                WsExceptionTemplate.REQUEST_PARAMETER_PAYLOAD_SPECIFICATION_ERROR, payloadSpecification
                        )));
                // sequence, version, code.
                if (payload.containsKey(name)) {
                    return payload.getObject(name, type);
                }

                final JSONObject payloadParams = Optional.ofNullable(payload.getJSONObject(payloadParamsDecodeName))
                        .orElseThrow(() -> new WsRequestParamException(String.format(
                                WsExceptionTemplate.REQUEST_PARAMETER_PAYLOAD_PARAMS_SPECIFICATION_ERROR, payloadParamSpecification
                        )));
                // payload params require to contain parameter ?
                Assert.checkXorCondition(!payloadParams.containsKey(name) && require,
                        () -> new WsRequestParamException(String.format(
                                WsExceptionTemplate.REQUEST_PARAMETER_NONE_MATCH, method.getName(), name)
                        )
                );
                return payloadParams.getObject(name, type);
            }
            if (org.springframework.util.TypeUtils.isAssignable(Map.class, type)) {
                return JSON.parseObject(textMessage.getPayload(), Map.class);
            }
            if (org.springframework.util.TypeUtils.isAssignable(payloadDecodeClazz, type)) {
                return JSON.parseObject(textMessage.getPayload(), payloadDecodeClazz);
            }
            final JSONObject payload = Optional.ofNullable(JSON.parseObject(textMessage.getPayload()))
                    .orElseThrow(() -> new WsRequestParamException(String.format(
                            WsExceptionTemplate.REQUEST_PARAMETER_PAYLOAD_SPECIFICATION_ERROR, payloadSpecification
                    )));
            return payload.getObject(payloadParamsDecodeName, type);
        }


        // Binary message
        if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, webSocketMessage.getClass())) {
            final BinaryMessage binaryMessage = TypeUtils.cast(webSocketMessage, BinaryMessage.class, ParserConfig.getGlobalInstance());

            if (org.springframework.util.TypeUtils.isAssignable(ByteBuffer.class, type)) {
                return binaryMessage.getPayload();
            }
            if (org.springframework.util.TypeUtils.isAssignable(ByteBuf.class, type)) {
                return ByteBufAllocator.DEFAULT.directBuffer().readBytes(binaryMessage.getPayload());
            }
            if (org.springframework.util.TypeUtils.isAssignable(byte[].class, type)) {
                return binaryMessage.getPayload().array();
            }
            return binaryMessage;
        }


        if (org.springframework.util.TypeUtils.isAssignable(PingMessage.class, type)) {
            return webSocketMessage;
        }
        if (org.springframework.util.TypeUtils.isAssignable(PongMessage.class, type)) {
            return webSocketMessage;
        }

        throw new WsException(WsBusiCode.INTERNAL_ERROR, "IGNORE");
    }
}
