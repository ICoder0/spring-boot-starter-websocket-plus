package com.icoder0.websocket.spring.handler.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.core.utils.Assert;
import com.icoder0.websocket.core.utils.SpelUtils;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.socket.*;

import org.springframework.validation.Validator;

import javax.validation.ValidationException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/17
 */
@Builder
public class WsMappingHandlerMethodMetadata {
    /* spel-expressions */
    private final String[] value;

    /* websocket-plus properties */
    private final Class<?> outerDecodeClazz;
    private final String spelRootName;

    private final List<WsMappingHandlerMethodParameterMetadata> parameters;

    @Getter
    private final Method method;

    @Getter
    private final Object bean;

    private final Validator validator;

    public Object[] extractArgs(WebSocketSession session, WebSocketMessage<?> message) {
        _checkSpelValid(message);
        Object[] args = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            final Object validateBean = parameters.get(i).extractArg(session, message);
            final BindException errors = new BindException(validateBean, "validateBean");
            ValidationUtils.invokeValidator(validator, validateBean, errors);
            final String errorJsonMessage = errors.getFieldErrors().parallelStream()
                    .map(fieldError -> fieldError.getField() + fieldError.getDefaultMessage())
                    .limit(1)
                    .collect(Collectors.joining());
            if (errors.hasErrors()) {
                throw new ValidationException(errorJsonMessage);
            }
            args[i] = validateBean;
        }
        return args;
    }

    void _checkSpelValid(WebSocketMessage<?> message) {
        if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, message.getClass())) {
            return;
        }
        if (org.springframework.util.TypeUtils.isAssignable(PingMessage.class, message.getClass())) {
            return;
        }
        if (org.springframework.util.TypeUtils.isAssignable(PongMessage.class, message.getClass())) {
            return;
        }
        final TextMessage textMessage = TypeUtils.cast(message, TextMessage.class, ParserConfig.getGlobalInstance());
        final Object validateBean = JSON.parseObject(textMessage.getPayload(), outerDecodeClazz);

        int index = 0;
        boolean exprMatched = false;
        do {
            exprMatched = SpelUtils.builder().context(spelRootName, validateBean).expr(value[index++]).getBooleanResult();
            if (exprMatched) {
                break;
            }
        } while (index < value.length);
        Assert.checkCondition(exprMatched, (Supplier<WsSpelValidationException>) WsSpelValidationException::new);
    }
}
