package com.icoder0.websocket.spring.exception;

import com.alibaba.fastjson.JSONException;
import com.icoder0.websocket.annotation.WebsocketAdvice;
import com.icoder0.websocket.annotation.WebsocketExceptionHandler;
import com.icoder0.websocket.core.constant.WsAttributeConstant;
import com.icoder0.websocket.core.constant.WsNativeTopic;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.exception.WsSpecificationException;
import com.icoder0.websocket.core.constant.WsBusiCode;
import com.icoder0.websocket.core.model.WsOutboundBean;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.ValidationException;

/**
 * @author bofa1ex
 * @since 2020/8/9
 */
@Slf4j
@WebsocketAdvice("com.icoder0")
public class DefaultWsGlobalExceptionAdvice {

    @WebsocketExceptionHandler(value = Throwable.class, priority = Integer.MIN_VALUE)
    public void handleRootException(WebSocketSession session, Throwable e) {
        log.error("[Throwable]异常: ", e);
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(WsBusiCode.INTERNAL_ERROR)
                .message(e.getMessage()).build(), session
        );
    }

    @WebsocketExceptionHandler(value = WsException.class, priority = Integer.MIN_VALUE + 1)
    public void handleWsException(WebSocketSession session, WsException e) {
        log.error("[WsException]异常: {}", e.getMessage());
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(e.getWsBusiCode())
                .message(e.getMessage()).build(), session
        );
    }

    @WebsocketExceptionHandler(WsSpecificationException.class)
    public void handleWsSpecificationException(WebSocketSession session, WsSpecificationException e) {
        log.error("[WsSpecificationException]异常: {}", e.getMessage());
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(e.getWsBusiCode())
                .message(e.getMessage()).build(), session
        );
    }

    @WebsocketExceptionHandler(JSONException.class)
    public void handleJsonException(WebSocketSession session, JSONException e) {
        log.error("[JSONException]异常: {}", e.getMessage());
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message(String.format("json解析失败, %s", e.getMessage())).build(), session
        );
    }

    @WebsocketExceptionHandler(ValidationException.class)
    public void handleValidationException(WebSocketSession session, ValidationException e) {
        log.error("[ValidationException]异常: {}", e.getMessage());
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message(String.format("violation校验失败, %s", e.getMessage())).build(), session
        );
    }

    @WebsocketExceptionHandler(SpelEvaluationException.class)
    public void handleSpelEvaluationException(WebSocketSession session, SpelEvaluationException e) {
        log.error("[SpelEvaluationException]异常: {}", e.getMessage());
        final Long topic = (Long) session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(topic)
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message(String.format("spel解析失败, %s", e.getMessage())).build(), session
        );
    }
}
