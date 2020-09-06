package com.icoder0.websocket.spring.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
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

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
@Slf4j
public class DefaultWebsocketMessageAspectHandler implements WebsocketMessageAspectHandler {

    @Resource
    protected WebsocketPlusProperties websocketPlusProperties;

    @Override
    public void handleInboundMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("{} INBOUND {}", session.getRemoteAddress(), message.getPayload());
        final Class<? extends WsInboundBeanSpecification> inboundBeanClazz = WebsocketPlusProperties.inboundBeanClazz;
        final String inboundSpecification           = WebsocketPlusProperties.inboundSpecification;
        final String payloadParamsDecodeName        = WebsocketPlusProperties.payloadParamsDecodeName;
        final String payloadFunctionCodeDecodeName  = WebsocketPlusProperties.payloadFunctionCodeDecodeName;
        final String payloadSequenceDecodeName      = WebsocketPlusProperties.payloadSequenceDecodeName;

        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, message.getClass())) {
            final TextMessage textMessage = TypeUtils.cast(message, TextMessage.class, ParserConfig.getGlobalInstance());
            final JSONObject payload = JSON.parseObject(textMessage.getPayload());
            // check inbound bean specification.
            Assert.checkXorCondition(payload.isEmpty() || Objects.isNull(payload.toJavaObject(inboundBeanClazz)), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_INBOUND_SPECIFICATION_ERROR, inboundSpecification
            )));
            // check inbound bean#sequence specification.
            Assert.checkCondition(payload.containsKey(payloadSequenceDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_SEQUENCE_SPECIFICATION_ERROR, payloadSequenceDecodeName
            )));
            // check inbound bean#params specification.
            Assert.checkCondition(payload.containsKey(payloadParamsDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_PARAMS_SPECIFICATION_ERROR, payloadParamsDecodeName
            )));
            // check inbound bean#functionCode specification.
            Assert.checkCondition(payload.containsKey(payloadFunctionCodeDecodeName), () -> new WsSpecificationException(String.format(
                    WsExceptionTemplate.REQUEST_PARAMETER_HEADER_FUNCTION_CODE_SPECIFICATION_ERROR, payloadFunctionCodeDecodeName
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
