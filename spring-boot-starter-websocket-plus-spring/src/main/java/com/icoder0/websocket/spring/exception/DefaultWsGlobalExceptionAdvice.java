package com.icoder0.websocket.spring.exception;

import com.alibaba.fastjson.JSONException;
import com.icoder0.websocket.annotation.WebsocketAdvice;
import com.icoder0.websocket.annotation.WebsocketExceptionHandler;
import com.icoder0.websocket.core.exception.WsBusiCode;
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

    @WebsocketExceptionHandler(value = RuntimeException.class, priority = Integer.MIN_VALUE)
    public void handleRuntimeException(WebSocketSession session, RuntimeException e) {
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.INTERNAL_ERROR)
                .message(e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(JSONException.class)
    public void handleJsonException(WebSocketSession session, JSONException e) {
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("json解析有误, " + e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(ValidationException.class)
    public void handleValidationException(WebSocketSession session, ValidationException e) {
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("校验失败, " + e.getMessage()), session
        );
    }

    @WebsocketExceptionHandler(SpelEvaluationException.class)
    public void handleSpelEvaluationException(WebSocketSession session, SpelEvaluationException e) {
        WebsocketMessageEmitter.emit(WsOutboundBean
                .status(WsBusiCode.ILLEGAL_REQUEST_ERROR)
                .message("spel解析有误, " + e.getMessage()), session
        );
    }
}
