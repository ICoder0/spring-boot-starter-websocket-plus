package com.icoder0.websocket.spring.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.constant.WsBusiCode;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import io.netty.buffer.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.*;

import java.nio.ByteBuffer;

/**
 * @author bofa1ex
 * @since 2020/8/28
 */
@Slf4j
@Builder
public class WsMappingHandlerMethodParameterMetadata {

    @Getter
    private final String name;
    @Getter
    private final boolean validated;
    private final boolean required;
    private final Class<?> type;
    private final String defaultValue;

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
            final TextMessage textMessage = TypeUtils.castToJavaBean(webSocketMessage, TextMessage.class);
            final JSONObject payload = JSON.parseObject(textMessage.getPayload());
            final JSONObject payloadParams = payload.getJSONObject(WebsocketPlusProperties.payloadParamsDecodeName);
            // 非基本数据类型&字符串类型直接由payload#params转换.
            if (! (ClassUtils.isPrimitiveOrWrapper(type) || org.springframework.util.TypeUtils.isAssignable(CharSequence.class, type))){
                return payload.getObject(WebsocketPlusProperties.payloadParamsDecodeName, type);
            }
            // 优先从payload层级提取数据.
            if (payload.containsKey(name)){
                if (payloadParams.containsKey(name) && log.isWarnEnabled()){
                    log.warn("{} 存在重复字段 {}, 优先考虑提取外层字段值 {}", session.getRemoteAddress(), name, payload);
                }
                return payload.getObject(name, type);
            }
            // 从payload#params层级提取, 并判断是否需要设置默认值(require&defaultValue).
            if (payloadParams.containsKey(name)){
                final Object arg = payloadParams.getObject(name, type);
                return arg == null && required ? TypeUtils.cast(defaultValue, type, ParserConfig.getGlobalInstance()) : arg;
            }
            // 如果payload未传递指定字段名, 按默认值分配, 反之返回null.
            return required ? TypeUtils.cast(defaultValue, type, ParserConfig.getGlobalInstance()) : null;
        }

        if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, webSocketMessage.getClass())) {
            final BinaryMessage binaryMessage = TypeUtils.castToJavaBean(webSocketMessage, BinaryMessage.class);

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
