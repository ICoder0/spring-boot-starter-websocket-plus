package com.icoder0.websocket.spring.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.exception.WsExceptionTemplate;
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
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/17
 */
@Builder
public class WsMappingHandlerMethodMetadata {

    private static final Validator validator;

    /* spel-expressions */
    private final String[] expressions;

    /* websocket-plus properties */
    private final Class<?> inboundBeanClazz;
    private final String spelVariableName;

    @Getter
    private final Method method;
    @Getter
    private final Object bean;
    private final List<WsMappingHandlerMethodParameterMetadata> parameters;

    public Object[] extractArgs(WebSocketSession session, WebSocketMessage<?> message) {
        // 校验message是否匹配spel#expression
        _checkSpelValid(message);
        Object[] args = new Object[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            final WsMappingHandlerMethodParameterMetadata parameter = parameters.get(i);
            // message根据parameter提取有效数据.
            final Object validateBean = parameter.extractArg(session, message);
            if (parameter.isValidated()) {
                // 检查提取出的有效数据是否符合constraints要求.
                Optional.ofNullable(validator.validate(validateBean)).ifPresent(this::_checkViolations);
            }
            args[i] = validateBean;
        }
        // 检查该方法提取出的args是否符合constraints要求.
        Optional.ofNullable(validator.forExecutables().validateParameters(bean, method, args)).ifPresent(this::_checkViolations);
        return args;
    }

    void _checkViolations(Set<ConstraintViolation<Object>> constraintViolations) {
        final String errorJsonMessage = constraintViolations.parallelStream()
                .map(constraintViolation -> String.format(
                        WsExceptionTemplate.CONSTRAINT_VIOLATION_VALIDATE_ERROR,
                        constraintViolation.getPropertyPath(),
                        constraintViolation.getMessage()))
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
        final Object validateBean = JSON.parseObject(textMessage.getPayload(), inboundBeanClazz);
        int index = 0;
        boolean exprMatched = false;
        do {
            exprMatched = SpelUtils.builder().context(spelVariableName, validateBean).expr(expressions[index++]).getBooleanResult();
            if (!exprMatched) {
                break;
            }
        } while (index < expressions.length);
        Assert.checkCondition(exprMatched, WsSpelValidationException::new);
    }

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
}
