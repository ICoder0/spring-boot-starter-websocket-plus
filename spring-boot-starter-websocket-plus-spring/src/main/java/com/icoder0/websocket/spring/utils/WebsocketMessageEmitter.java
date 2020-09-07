package com.icoder0.websocket.spring.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.constant.WsAttributeConstant;
import com.icoder0.websocket.core.model.WsOutboundBeanSpecification;
import com.icoder0.websocket.spring.WebsocketPlusProperties;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Slf4j
@UtilityClass
public class WebsocketMessageEmitter {

    /**
     * 下发消息序号与上行数据一致.
     */
    public <T extends WsOutboundBeanSpecification> void emitAuto(T data, WebSocketSession session) {
        _autoInjectSequence(data, session);
        final String json = JSON.toJSONString(data);
        log.info("{} OUTBOUND {}", session.getRemoteAddress(), json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("{} send message {} error", session, data);
        }
    }

    public <T extends WsOutboundBeanSpecification> void emitIncr(T data, WebSocketSession session) {
        _autoInjectIncrementSequence(data, session);
        final String json = JSON.toJSONString(data);
        log.info("{} OUTBOUND {}", session.getRemoteAddress(), json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("{} send message {} error", session, data);
        }
    }

    @SuppressWarnings("不建议使用, 尽可能走#emit#检查下行数据格式约束")
    public void emitIgnore(Object data, WebSocketSession session) {
        final String json = JSON.toJSONString(data);
        log.info("{} OUTBOUND {}", session.getRemoteAddress(), json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("{} send message {} error", session, data);
        }
    }

    <T extends WsOutboundBeanSpecification> void _autoInjectSequence(T data, WebSocketSession session) {
        /* @see DefaultWebsocketMessageCustomizer#customize, 默认在原生缓存attributes中注入了上行数据得到的函数枚举和消息序号. */
        data.setSequence(Optional.ofNullable(data.sequence()).orElse(
                TypeUtils.castToLong(session.getAttributes().getOrDefault(
                        WsAttributeConstant.IMMUTABLE_SEQUENCE, 0L
                ))
        ));
    }

    <T extends WsOutboundBeanSpecification> void _autoInjectIncrementSequence(T data, WebSocketSession session) {
        /* @see DefaultWebsocketMessageCustomizer#customize, 默认在原生缓存attributes中注入了上行数据得到的函数枚举和消息序号. */
        data.setSequence(Optional.ofNullable(data.sequence()).orElse(
                TypeUtils.castToJavaBean(session.getAttributes().getOrDefault(
                        WsAttributeConstant.VARIABLE_SEQUENCE, new AtomicLong()
                ), AtomicLong.class).incrementAndGet())
        );
    }

}
