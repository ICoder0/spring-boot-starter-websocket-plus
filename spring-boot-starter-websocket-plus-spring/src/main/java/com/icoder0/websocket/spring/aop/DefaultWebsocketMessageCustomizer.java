package com.icoder0.websocket.spring.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.constant.WsAttributeConstant;
import com.icoder0.websocket.core.model.WsInboundBeanSpecification;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author bofa1ex
 * @since 2020/9/6
 */
public class DefaultWebsocketMessageCustomizer implements WebsocketMessageCustomizer {

    @Override
    public void customize(WebSocketSession session, WebSocketMessage<?> message) {
        if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, message.getClass())) {
            final TextMessage textMessage = TypeUtils.castToJavaBean(message, TextMessage.class);
            final WsInboundBeanSpecification wsInboundBeanSpecification = JSON.parseObject(textMessage.getPayload(), WebsocketPlusProperties.inboundBeanClazz);

            session.getAttributes().put(WsAttributeConstant.IMMUTABLE_SEQUENCE, wsInboundBeanSpecification.sequence());
            session.getAttributes().put(WsAttributeConstant.VARIABLE_SEQUENCE, new AtomicLong(wsInboundBeanSpecification.sequence()));
            session.getAttributes().put(WsAttributeConstant.FUNCTION_CODE, wsInboundBeanSpecification.functionCode());
        }
    }
}
