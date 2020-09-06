package com.icoder0.websocket.spring.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.model.WsBusiCode;
import io.netty.buffer.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.*;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * @author bofa1ex
 * @since 2020/8/28
 */
@Builder
public class WsMappingHandlerMethodParameterMetadata {

    private final Class<?> inboundBeanClazz;

    private final String payloadParamsDecodeName;

    private final Method method;
    @Getter
    private final String name;
    private final String defaultValue;
    private final boolean require;

    private final Class<?> type;
    @Getter
    private final boolean validated;

    /* method-parameter state */
    private final boolean isHeader;
    private final boolean isNormal;

    /**
     * 只考虑抽取的逻辑.
     * 上行数据格式规范的校验和异常处理在aop#handleInboundMessage入口解决.
     */
    public Object extractArg(WebSocketSession session, WebSocketMessage<?> webSocketMessage) {
        if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, type)) {
            return session;
        }
        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, webSocketMessage.getClass())) {
            if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, type)) {
                return webSocketMessage;
            }
            final TextMessage textMessage = TypeUtils.cast(webSocketMessage, TextMessage.class, ParserConfig.getGlobalInstance());
            final JSONObject payload = JSON.parseObject(textMessage.getPayload());
            final JSONObject payloadParams = payload.getJSONObject(payloadParamsDecodeName);
            return isNormal ?
                    ClassUtils.isPrimitiveOrWrapper(type) || org.springframework.util.TypeUtils.isAssignable(CharSequence.class, type) ?
                    // 如果没有@WebsocketPayload和@WebsocketHeader修饰
                    // 如果是基本类型或者CharSequence类型, 则从payload#params中提取, 反之Map,Pojo类型从payload当前层级提取.
                    payloadParams.getObject(name, type) : payload.getObject(payloadParamsDecodeName, type) : isHeader ?
                    // 如果是@WebsocketHeader, 从payload当前层级中提取, 反之从payload#params中提取.
                    payload.getObject(name, type) : payloadParams.getObject(name, type);
        }

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
