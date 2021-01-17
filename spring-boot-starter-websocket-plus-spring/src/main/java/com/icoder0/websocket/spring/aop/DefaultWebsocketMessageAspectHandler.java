package com.icoder0.websocket.spring.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.constant.WsAttributeConstant;
import com.icoder0.websocket.core.exception.WsExceptionTemplate;
import com.icoder0.websocket.core.exception.WsSpecificationException;
import com.icoder0.websocket.core.model.WsInboundBeanSpecification;
import com.icoder0.websocket.core.model.WsOutboundBeanSpecification;
import com.icoder0.websocket.core.utils.Assert;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
@Slf4j
public class DefaultWebsocketMessageAspectHandler implements WebsocketMessageAspectHandler {

    /**
     * 不建议用户复写该方法, 可以在WebsocketMessageCustomizer#
     * @param session
     * @param message
     */
    @Override
    public final void handleInboundMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress(), message.getPayload());
        final Class<? extends WsInboundBeanSpecification> inboundBeanClazz = WebsocketPlusProperties.inboundBeanClazz;
        final String inboundSpecification           = WebsocketPlusProperties.inboundSpecification;
        final String payloadParamsDecodeName        = WebsocketPlusProperties.payloadParamsDecodeName;
        final String payloadTopicDecodeName         = WebsocketPlusProperties.payloadTopicDecodeName;
        final String payloadSequenceDecodeName      = WebsocketPlusProperties.payloadSequenceDecodeName;

        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, message.getClass())) {
            final TextMessage textMessage = TypeUtils.castToJavaBean(message, TextMessage.class);
            final JSONObject payload = Optional.ofNullable(JSON.parseObject(textMessage.getPayload())).orElseGet(JSONObject::new);

            // check inbound bean specification.
            Assert.checkXorCondition(payload.isEmpty() || Objects.isNull(payload.toJavaObject(inboundBeanClazz)), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_INBOUND_SPECIFICATION_ERROR, inboundSpecification
            )));
            // check inbound bean#sequence specification.
            Assert.checkCondition(payload.containsKey(payloadSequenceDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_SEQUENCE_SPECIFICATION_ERROR, payloadSequenceDecodeName
            )));
            // bind sequence in session attributes.
            session.getAttributes().compute(WsAttributeConstant.SEQUENCE, (ignore, v) -> {
                final long sequence = payload.getLongValue(payloadSequenceDecodeName);
                if (v == null){
                    v = new AtomicLong(sequence);
                }
                TypeUtils.castToJavaBean(v, AtomicLong.class).set(sequence);
                return v;
            });
            // check inbound bean#topic specification.
            Assert.checkCondition(payload.containsKey(payloadTopicDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_TOPIC_SPECIFICATION_ERROR, payloadTopicDecodeName
            )));
            // bind topic in session attributes.
            session.getAttributes().put(WsAttributeConstant.TOPIC, payload.getLongValue(payloadTopicDecodeName));
            // check inbound bean#params specification.
            Assert.checkCondition(payload.containsKey(payloadParamsDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_PARAMS_SPECIFICATION_ERROR, payloadParamsDecodeName
            )));
        }
    }

    @Override
    public void handleOutboundMessage(WebSocketSession session, WsOutboundBeanSpecification outboundBean) {
        if (Objects.isNull(outboundBean)) {
            log.warn("{} OUTBOUND IS NULL", session.getRemoteAddress());
            return;
        }
        // 如果复写该方法, 切记一定需要提供下发下行数据的逻辑.
        WebsocketMessageEmitter.emit(outboundBean, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (log.isWarnEnabled()) {
            log.warn("{} 断开连接", session.getRemoteAddress());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("{} 建立连接", session.getRemoteAddress());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
        if (log.isErrorEnabled()) {
            log.error("{} 传输出错", session.getRemoteAddress(), e);
        }
    }
}
