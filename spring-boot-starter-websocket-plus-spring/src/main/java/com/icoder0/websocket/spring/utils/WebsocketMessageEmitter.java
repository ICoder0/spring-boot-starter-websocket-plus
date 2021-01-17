package com.icoder0.websocket.spring.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.icoder0.websocket.core.constant.WsAttributeConstant;
import com.icoder0.websocket.core.constant.WsNativeTopic;
import com.icoder0.websocket.core.model.WsOutboundBeanSpecification;
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
    public <T extends WsOutboundBeanSpecification> void emit(T data, WebSocketSession session) {
        /* @see DefaultWebsocketMessageCustomizer#customize, 默认在原生缓存attributes中注入了上行数据得到的订阅主题和消息序号. */
        if (data.sequence() == null) {
            AtomicLong sequence = TypeUtils.castToJavaBean(session.getAttributes().get(WsAttributeConstant.SEQUENCE), AtomicLong.class);
            if (sequence == null) {
                sequence = new AtomicLong(-1L);
                session.getAttributes().putIfAbsent(WsAttributeConstant.SEQUENCE, sequence);
            }
            data.setSequence(sequence.incrementAndGet());
        }
        if (data.topic() == null) {
            data.setTopic(TypeUtils.castToLong(session.getAttributes().getOrDefault(WsAttributeConstant.TOPIC, WsNativeTopic.NO_TOPIC.topic)));
        }
        final String json = JSON.toJSONString(data);
        log.info("{} OUTBOUND {}", session.getRemoteAddress(), json);
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("{} send message {} error", session, data);
        }
    }


    @Deprecated
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
}
