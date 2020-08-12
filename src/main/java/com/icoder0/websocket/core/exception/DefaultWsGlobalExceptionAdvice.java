package com.icoder0.websocket.core.exception;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.core.annotation.WebsocketAdvice;
import com.icoder0.websocket.core.annotation.WebsocketExceptionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.ValidationException;
import java.io.IOException;

/**
 * @author bofa1ex
 * @since 2020/8/9
 */
@Slf4j
@WebsocketAdvice("com.icoder0")
public class DefaultWsGlobalExceptionAdvice {

    @WebsocketExceptionHandler(RuntimeException.class)
    public void handleRuntimeException(WebSocketSession session, RuntimeException e) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(ImmutableMap.of(
                "code", 500,
                "message", e.getMessage()
        ))));
    }

    @WebsocketExceptionHandler(JSONException.class)
    public void handleJsonException(WebSocketSession session, JSONException e) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(ImmutableMap.of(
                "code", 400,
                "message", "json解析有误, " + e.getMessage()
        ))));
    }

    @WebsocketExceptionHandler(ValidationException.class)
    public void handleValidationException(WebSocketSession session, ValidationException e) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(ImmutableMap.of(
                "code", 410,
                "message", "校验失败, " + e.getMessage()
        ))));
    }

    @WebsocketExceptionHandler(SpelEvaluationException.class)
    public void handleSpelEvaluationException(WebSocketSession session, SpelEvaluationException e) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(ImmutableMap.of(
                "code", 411,
                "message", "spel解析有误, " + e.getMessage()
        ))));
    }
}
