package com.icoder0.websocket.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.core.utils.Assert;
import com.icoder0.websocket.core.utils.SpelUtils;
import com.icoder0.websocket.spring.handler.WsExceptionHandler;
import com.icoder0.websocket.spring.handler.WsSpelValidationHandler;
import com.icoder0.websocket.spring.handler.model.WsExceptionHandlerMethodMetadata;
import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMethodMetadata;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.*;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Data
@Slf4j
public class WebsocketArchetypeHandler implements WsExceptionHandler, WebSocketHandler, WsSpelValidationHandler {

    final String MVC_VALIDATOR_NAME = "mvcValidator";

    @Resource(name = MVC_VALIDATOR_NAME, type = Validator.class)
    private Validator validator;

    @Resource
    private WebsocketPlusProperties websocketPlusProperties;

    private List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList;

    private List<WsExceptionHandlerMethodMetadata> exceptionMethodMetadataList;

    private String location;

    @Override
    public void handleException(WebSocketSession session, Throwable t) {
        final List<WsExceptionHandlerMethodMetadata> exceptionHandlerMethodMetadataList = this.getExceptionMethodMetadataList().parallelStream()
                .filter(_metadata -> org.springframework.util.TypeUtils.isAssignable(_metadata.getValue(), t.getClass()))
                .collect(Collectors.toList());
        if (exceptionHandlerMethodMetadataList.isEmpty()) {
            log.warn("该异常类型{} 没有被正确处理", t.getClass().getSimpleName());
            return;
        }
        exceptionHandlerMethodMetadataList.parallelStream().max(Comparator.comparing(WsExceptionHandlerMethodMetadata::getPriority)).ifPresent(metadata -> {
            final Method method = metadata.getMethod();
            final Object[] args = Arrays.stream(method.getParameters()).parallel().map(parameter ->
                    org.springframework.util.TypeUtils.isAssignable(Exception.class, parameter.getType()) ?
                            t : org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameter.getType()) ?
                            session : null
            ).toArray(Object[]::new);
            ReflectionUtils.invokeMethod(metadata.getMethod(), metadata.getBean(), args);
        });
    }


    @Override
    public final void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        final Class<?> outerDecodeClazz = this.getWebsocketPlusProperties().getOuterDecodeClazz();
        int semaphores = this.getMappingMethodMetadataList().size();
        for (WsMappingHandlerMethodMetadata wsMappingHandlerMethodMetadata : this.getMappingMethodMetadataList()) {
            final String[] spelExpressions = wsMappingHandlerMethodMetadata.getValue();
            final Method method = wsMappingHandlerMethodMetadata.getMethod();
            final Object target = wsMappingHandlerMethodMetadata.getBean();
            try {
                /* 暂时先不处理除TextMessage以外的类型数据, 没这方面的需求 */
                if (message instanceof TextMessage) {
                    final TextMessage textMessage = TypeUtils.cast(message, TextMessage.class, ParserConfig.getGlobalInstance());
                    validate(JSON.parseObject(textMessage.getPayload(), outerDecodeClazz), spelExpressions);
                    final Object[] args = processMethodParameters(method.getParameters(), session, textMessage);
                    final Object outboundBean = ReflectionUtils.invokeMethod(method, target, args);
                    if (Objects.isNull(outboundBean)) {
                        log.warn("no result found after invoke {}", method);
                        return;
                    }
                    log.info("[{}] send message {}", session.getRemoteAddress() + "@" + session.getId(), outboundBean);
                    WebsocketMessageEmitter.emit(outboundBean, session);
                }
            } catch (WsSpelValidationException ignored) {
                semaphores -= 1;
            } catch (Exception e) {
                handleException(session, e);
                break;
            }
        }
        Assert.checkXorCondition(semaphores == 0, () -> WebsocketMessageEmitter.emit(
                ImmutableMap.of(
                        "code", "400",
                        "message", "请检查inbound message规范"
                ), session)
        );
    }

    @Override
    public void validate(Object inboundBean, String... spelExpressions) {
        final String spelRootName = this.getWebsocketPlusProperties().getSpelRootName();
        int index = 0;
        boolean exprMatched = false;
        do {
            exprMatched = SpelUtils.builder().context(spelRootName, inboundBean).expr(spelExpressions[index++]).getBooleanResult();
            if (exprMatched) {
                break;
            }
        } while (index < spelExpressions.length);
        if (!exprMatched) {
            throw new WsSpelValidationException();
        }
    }

    public Object[] processMethodParameters(Parameter[] parameters, WebSocketSession webSocketSession, TextMessage textMessage) {
        final Object[] args = new Object[parameters.length];
        final Class<?> outerDecodeClazz = this.getWebsocketPlusProperties().getOuterDecodeClazz();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterType = parameter.getType();
            if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, parameterType)) {
                args[i] = textMessage;
                continue;
            }
            if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameterType)) {
                args[i] = webSocketSession;
                continue;
            }
            if (org.springframework.util.TypeUtils.isAssignable(Map.class, parameterType)) {
                args[i] = JSON.parseObject(textMessage.getPayload(), Map.class);
                continue;
            }
            if (org.springframework.util.TypeUtils.isAssignable(outerDecodeClazz, parameterType)) {
                args[i] = JSON.parseObject(textMessage.getPayload());
                continue;
            }
            Object innerInboundBean = JSON.parseObject(textMessage.getPayload()).getObject(this.getWebsocketPlusProperties().getInnerDecodeParamKeyName(), parameterType);
            if (AnnotatedElementUtils.hasAnnotation(parameter, Validated.class)) {
                final BindException errors = new BindException(innerInboundBean, "innerInboundBean");
                ValidationUtils.invokeValidator(validator, innerInboundBean, errors);
                final String errorJsonMessage = errors.getFieldErrors().parallelStream()
                        .map(fieldError -> fieldError.getField() + fieldError.getDefaultMessage())
                        .limit(1)
                        .collect(Collectors.joining());
                if (errors.hasErrors()) {
                    throw new ValidationException(errorJsonMessage);
                }
            }
            args[i] = innerInboundBean;
        }
        return args;
    }

    @Override
    public final void handleTransportError(WebSocketSession session, Throwable e) {
        handleException(session, e);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void addExceptionMethodMetadataList(List<WsExceptionHandlerMethodMetadata> exceptionMethodMetadataList) {
        if (this.getExceptionMethodMetadataList() == null) {
            this.setExceptionMethodMetadataList(exceptionMethodMetadataList);
            return;
        }
        this.getExceptionMethodMetadataList().addAll(exceptionMethodMetadataList);
    }

    @Override
    public String toString() {
        return "archetypeHandler@" + this.hashCode();
    }
}
