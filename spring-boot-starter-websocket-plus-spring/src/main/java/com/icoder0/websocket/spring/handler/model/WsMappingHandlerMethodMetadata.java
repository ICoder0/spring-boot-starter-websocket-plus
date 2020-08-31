package com.icoder0.websocket.spring.handler.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.core.utils.Assert;
import com.icoder0.websocket.core.utils.SpelUtils;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.*;


import javax.validation.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private static final Validator validator;


    public Object[] extractArgs(WebSocketSession session, WebSocketMessage<?> message) {
        _checkSpelValid(message);
        Object[] args = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            final WsMappingHandlerMethodParameterMetadata parameter = parameters.get(i);
            final Object validateBean = parameter.extractArg(session, message);
            if (parameter.isValidated()) {
                Optional.ofNullable(validator.validate(validateBean)).ifPresent(this::_checkViolations);
            }
            args[i] = validateBean;
        }
        Optional.ofNullable(validator.forExecutables().validateParameters(bean, method, args)).ifPresent(this::_checkViolations);
        return args;
    }

    void _checkViolations(Set<ConstraintViolation<Object>> constraintViolations) {
        final String errorJsonMessage = constraintViolations.parallelStream()
                .map(constraintViolation -> constraintViolation.getRootBean().toString() + "#" +
                        constraintViolation.getPropertyPath() + ":=" + constraintViolation.getInvalidValue() + " IS NOT VALID " +
                        "#REQUIRE {" + constraintViolation.getMessage() + "}"
                )
                .limit(1)
                .collect(Collectors.joining());
        if (!constraintViolations.isEmpty()) {
            throw new ValidationException(errorJsonMessage);
        }
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
            // 如果出现false, 则不符合condition.
            if (!exprMatched) {
                break;
            }
        } while (index < value.length);
        Assert.checkCondition(exprMatched, (Supplier<WsSpelValidationException>) WsSpelValidationException::new);
    }

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
}
