package com.icoder0.websocket.spring;

import com.icoder0.websocket.core.exception.WsBusiCode;
import com.icoder0.websocket.core.exception.WsException;
import com.icoder0.websocket.core.exception.WsExceptionTemplate;
import com.icoder0.websocket.core.exception.WsSpelValidationException;
import com.icoder0.websocket.spring.model.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/8/1
 */
@Data
@Slf4j
public class WebsocketArchetypeHandler implements WebSocketHandler {

    private List<WsMappingHandlerMethodMetadata> mappingMethodMetadataList;

    private List<WsExceptionHandlerMethodMetadata> exceptionMethodMetadataList;

    private String location;

    public final void handleException(WebSocketSession session, Throwable t) {
        this.getExceptionMethodMetadataList().parallelStream()
                .filter(_metadata -> org.springframework.util.TypeUtils.isAssignable(_metadata.getValue(), t.getClass()))
                // 按优先级最高的处理器
                .max(Comparator.comparing(WsExceptionHandlerMethodMetadata::getPriority)).ifPresent(metadata -> {
            final Method method = metadata.getMethod();
            final Object[] args = Arrays.stream(method.getParameters()).parallel().map(parameter ->
                    org.springframework.util.TypeUtils.isAssignable(Throwable.class, parameter.getType()) ?
                            t : org.springframework.util.TypeUtils.isAssignable(WebSocketSession.class, parameter.getType()) ?
                            session : null
            ).toArray(Object[]::new);
            ReflectionUtils.invokeMethod(metadata.getMethod(), metadata.getBean(), args);
        });
    }

    @Override
    public final void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        handleInboundMessage(session, message);
        for (WsMappingHandlerMethodMetadata wsMappingHandlerMethodMetadata : this.getMappingMethodMetadataList()) {
            final Method method = wsMappingHandlerMethodMetadata.getMethod();
            final Object target = wsMappingHandlerMethodMetadata.getBean();
            try {
                final Object[] args = wsMappingHandlerMethodMetadata.extractArgs(session, message);
                final Object outboundBean = ReflectionUtils.invokeMethod(method, target, args);
                handleOutboundMessage(session, outboundBean);
                return;
            } catch (WsSpelValidationException ignored) {
            } catch (Throwable requestParamException) {
                handleException(session, requestParamException);
                return;
            }
        }
        handleException(session, new WsException(WsBusiCode.ILLEGAL_REQUEST_ERROR, String.format(
                WsExceptionTemplate.METHOD_MAPPING_NONE_MATCH, message.getPayload()))
        );
    }

    public void handleInboundMessage(WebSocketSession session, WebSocketMessage<?> message) {

    }

    public void handleOutboundMessage(WebSocketSession session, Object outboundBean) {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
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
