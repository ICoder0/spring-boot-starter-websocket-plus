package com.icoder0.websocket.core.annotation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.core.handler.WsExceptionHandler;
import com.icoder0.websocket.core.handler.WsTokenHandler;
import com.icoder0.websocket.core.utils.SpelUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.*;

import javax.validation.ValidationException;
import javax.validation.groups.Default;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Slf4j
@Setter
@Getter
@Builder
public class WebsocketDelegatorHandler implements WsExceptionHandler, WebSocketHandler {

    private final String[] routeMapping;

    private final String location;

    private final Validator validator;

    private final List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList;

    private List<WsExceptionHandlerMethodMetadata> exceptionMethodMetadataList;

    private final WebsocketPlusProperties websocketPlusProperties;

    @Getter
    @Builder
    public static class WsExceptionHandlerMethodMetadata {
        private final Class<? extends Throwable> value;
        private final Method method;
        private final Object bean;
    }

    @Getter
    @Builder
    public static class WsMappingHandlerMethodMetadata {
        /* spel-expression */
        private final String[] value;
        private final Method method;
        private final Object bean;
    }

    @Override
    public void handleException(WebSocketSession session, Throwable t) {
        if (log.isErrorEnabled()) {
            log.error("[{}] {}", session.getId(), t.getMessage());
        }
        final Optional<WsExceptionHandlerMethodMetadata> metadataOptional = exceptionMethodMetadataList.parallelStream()
                .filter(_metadata -> org.springframework.util.TypeUtils.isAssignable(_metadata.getValue(), t.getClass()))
                .findFirst();
        if (!metadataOptional.isPresent()) {
            log.warn("遗漏的全局异常处理, 该异常{} 没有被正确处理", t.getMessage());
        }
        final WsExceptionHandlerMethodMetadata metadata = metadataOptional.get();
        final Method method = metadata.getMethod();
        final Object[] args = Arrays.stream(method.getParameters()).parallel()
                .map(parameter -> {
                    if (org.springframework.util.TypeUtils.isAssignable(Exception.class, parameter.getType())) {
                        return t;
                    }
                    if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameter.getType())) {
                        return session;
                    }
                    return null;
                })
                .toArray(Object[]::new);
        ReflectionUtils.invokeMethod(metadata.getMethod(), metadata.getBean(), args);
    }

    @Override
    public final void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("[{}] receive message {}", session.getRemoteAddress() + "@" + session.getId(), message.getPayload());
        for (WsMappingHandlerMethodMetadata websocketMappingHandlerWrapper : mappingMethodMetadataList) {
            final String[] spelExpressions = websocketMappingHandlerWrapper.getValue();
            final Method method = websocketMappingHandlerWrapper.getMethod();
            final Object target = websocketMappingHandlerWrapper.getBean();
            try {
                final Object[] args = processMethodParameters(method.getParameters(), session, message, spelExpressions);
                final Object outboundBean = ReflectionUtils.invokeMethod(method, target, args);
                if (Objects.isNull(outboundBean)) {
                    log.warn("no result found after invoke {}", method);
                    return;
                }
                log.info("[{}] send message {}", session.getRemoteAddress() + "@" + session.getId(), outboundBean);
                final String json = JSON.toJSONString(outboundBean);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                if (e instanceof WsSpelValidationException) {
                    continue;
                }
                handleException(session, e);
                break;
            }
        }
    }

    boolean _validateSpelExpr(Object inboundBean, String... spelExpressions) {
        final String spelRootName = websocketPlusProperties.getSpelRootName();
        int index = 0;
        boolean exprMatched = false;
        do {
            exprMatched = SpelUtils.builder().context(spelRootName, inboundBean).expr(spelExpressions[index++]).getBooleanResult();
            if (exprMatched) {
                break;
            }
        } while (index < spelExpressions.length);
        return exprMatched;
    }

    public Object[] processMethodParameters(Parameter[] parameters, WebSocketSession webSocketSession, WebSocketMessage<?> message, String... spelExprs) {
        final Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Class<?> parameterType = parameter.getType();
            final Class<?> outerDecodeClazz = this.websocketPlusProperties.getOuterDecodeClazz();
            // 该逻辑不应该出现在runtime阶段, 而是应该在compile阶段抛出异常。
            if (parameterType.isPrimitive() && org.springframework.util.TypeUtils.isAssignable(CharSequence.class, parameterType)) {
                log.warn("暂不支持{}入参为基本类型或者CharSequence类型, 请填写实体类用于json解析..", parameterType);
                throw new RuntimeException(String.format("illegal argument type: %s", parameterType));
            }
            if (org.springframework.util.TypeUtils.isAssignable(TextMessage.class, parameterType)) {
                args[i] = message;
                continue;
            }
            if (org.springframework.util.TypeUtils.isAssignable(BinaryMessage.class, parameterType)) {
                args[i] = message;
                continue;
            }
            Assert.isInstanceOf(TextMessage.class, message, String.format("illegal argument type: %s", parameterType));
            final TextMessage textMessage = TypeUtils.cast(message, TextMessage.class, ParserConfig.getGlobalInstance());
            Object inboundBean = JSON.parseObject(textMessage.getPayload(), outerDecodeClazz);
            if (!_validateSpelExpr(inboundBean, spelExprs)) {
//                log.warn("websocketMethodMapping#expr未匹配到项, 检查{}下的methods, 以及入参{}是否有误", Arrays.toString(routeMapping), inboundBean);
                throw new WsSpelValidationException();
            }
            if (org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameterType)) {
                args[i] = webSocketSession;
                continue;
            }
            if (org.springframework.util.TypeUtils.isAssignable(ByteBuffer.class, parameterType)) {
                Assert.isInstanceOf(BinaryMessage.class, message, String.format("illegal argument type: %s", parameterType));
                args[i] = ((BinaryMessage) message).getPayload();
            }
            if (org.springframework.util.TypeUtils.isAssignable(Map.class, parameterType)) {
                Assert.isInstanceOf(TextMessage.class, message, String.format("illegal argument type: %s", parameterType));
                args[i] = JSON.parseObject(((TextMessage) message).getPayload(), Map.class);
            }
            if (!org.springframework.util.TypeUtils.isAssignable(outerDecodeClazz, parameterType)) {
                inboundBean = JSON.parseObject(textMessage.getPayload()).getObject(this.websocketPlusProperties.getInnerDecodeParamKeyName(), parameterType);
            }
            if (AnnotatedElementUtils.hasAnnotation(parameter, Validated.class)) {
                final BindException errors = new BindException(inboundBean, "inboundBean");
                ValidationUtils.invokeValidator(validator, inboundBean, errors);
                final String errorJsonMessage = errors.getFieldErrors().parallelStream()
                        .map(fieldError -> fieldError.getField() + fieldError.getDefaultMessage())
                        .limit(1)
                        .collect(Collectors.joining());
                if (errors.hasErrors()) {
                    throw new ValidationException(errorJsonMessage);
                }
            }
            args[i] = inboundBean;
        }
        return args;
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[{}] 断开连接", session.getRemoteAddress() + "@" + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public final void handleTransportError(WebSocketSession session, Throwable e) {
        log.info("[{}] 传输出错", session.getRemoteAddress() + "@" + session.getId(), e);
        handleException(session, e);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (log.isInfoEnabled()) {
            log.info("[{}] 建立连接", session.getRemoteAddress() + "@" + session.getId());
        }
//        Assert.isAssignable(session.getClass(), StandardWebSocketSession.class, "未获取到NativeSession");
//        final Session nativeSession = ((StandardWebSocketSession) session).getNativeSession();
//        final Map<String, List<String>> requestParameterMap = nativeSession.getRequestParameterMap();
//        final String encryptToken = requestParameterMap.get("token").parallelStream().findFirst()
//                .orElseThrow(() -> new WsException(WsCode.RES_NOT_FOUND, "未获取到token"));
//        /* aes解密, 获取加密数据域和对称密钥 */
//        final String encryptHex = encryptToken.substring(0, encryptToken.indexOf("."));
//        final String encryptKeyHex = encryptToken.substring(encryptToken.indexOf(".") + 1);
//        byte[] decryptBytes = AESUtils.decrypt_cbc(ByteUtils.hex2Bytes(encryptHex), ByteUtils.hex2Bytes(encryptKeyHex), true);
//        final String token = StringUtils.newStringUtf8(decryptBytes);
//        consumeToken(token);
    }
}
