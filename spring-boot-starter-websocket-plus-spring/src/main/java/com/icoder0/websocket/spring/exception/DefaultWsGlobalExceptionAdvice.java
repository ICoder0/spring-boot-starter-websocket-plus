package com.icoder0.websocket.spring.exception;

import com.alibaba.fastjson.JSONException;
import com.icoder0.websocket.annotation.WebsocketAdvice;
import com.icoder0.websocket.annotation.WebsocketExceptionHandler;
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
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.INTERNAL_ERROR)
                .message(e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(value = WsException.class, priority = Integer.MIN_VALUE + 1)
    public void handleWsException(WebSocketSession session, WsException e) {
        log.error("[WsException]异常: {}", e.getMessage());
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(e.getWsBusiCode())
                .message(e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(WsSpecificationException.class)
    public void handleWsSpecificationException(WebSocketSession session, WsSpecificationException e) {
        log.error("[WsSpecificationException]异常: {}", e.getMessage());
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(e.getWsBusiCode())
                .message(e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(JSONException.class)
    public void handleJsonException(WebSocketSession session, JSONException e) {
        log.error("[JSONException]异常: {}", e.getMessage());
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("json解析失败, " + e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(ValidationException.class)
    public void handleValidationException(WebSocketSession session, ValidationException e) {
        log.error("[ValidationException]异常: {}", e.getMessage());
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("violation校验失败, " + e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(SpelEvaluationException.class)
    public void handleSpelEvaluationException(WebSocketSession session, SpelEvaluationException e) {
        log.error("[SpelEvaluationException]异常: {}", e.getMessage());
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("spel解析失败, " + e.getMessage()), session
        );
    }
}
