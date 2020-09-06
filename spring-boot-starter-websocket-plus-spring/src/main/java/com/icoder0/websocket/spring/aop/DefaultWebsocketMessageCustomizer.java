package com.icoder0.websocket.spring.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.model.WsInboundBeanSpecification;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * @author bofa1ex
 * @since 2020/9/6
 */
public class DefaultWebsocketMessageCustomizer implements WebsocketMessageCustomizer {

    @Resource
    protected WebsocketPlusProperties websocketPlusProperties;

    @Override
    public void customize(WebSocketSession session, WebSocketMessage<?> message) {
        final String payloadFunctionCodeDecodeName  = WebsocketPlusProperties.payloadFunctionCodeDecodeName;
        final String payloadSequenceDecodeName      = WebsocketPlusProperties.payloadSequenceDecodeName;

        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, message.getClass())) {
            final TextMessage textMessage = TypeUtils.cast(message, TextMessage.class, ParserConfig.getGlobalInstance());
            final WsInboundBeanSpecification wsInboundBeanSpecification = JSON.parseObject(textMessage.getPayload(), WebsocketPlusProperties.inboundBeanClazz);
            session.getAttributes().put(payloadSequenceDecodeName, wsInboundBeanSpecification.sequence());
            session.getAttributes().put(payloadFunctionCodeDecodeName, wsInboundBeanSpecification.functionCode());
        }
    }
}
