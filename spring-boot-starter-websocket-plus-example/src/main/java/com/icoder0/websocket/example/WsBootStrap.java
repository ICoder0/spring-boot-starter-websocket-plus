package com.icoder0.websocket.example;

import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.core.model.WsOutboundBean;
import com.icoder0.websocket.core.utils.ByteUtils;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


import static com.icoder0.websocket.example.WsExampleTopic.*;

/**
 * @author bofa1ex
 * @since 2020/7/31
 */
@Slf4j
@WebsocketMapping(value = "/api/mirai", prototype = true)
public class WsBootStrap {

    @WebsocketMethodMapping(value = "#inbound.topic == 1001")
    public WsOutboundBean<?> login(WebSocketSession webSocketSession, @Validated WsLoginVO req) {
        log.info("login {}", req);
        webSocketSession.getAttributes().put("account", req.getAccount());
        return WsOutboundBean.topic(TOPIC_1001.topic).ok(ImmutableMap.of(
                "hello", "world"
        ));
    }

    @WebsocketMethodMapping("#inbound.topic == 1002'")
    public void logout(WebSocketSession webSocketSession, @NotBlank @Size(min = 4, max = 6) String account) {
        log.info("{} logout", account);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(TOPIC_1002.topic).ok(ImmutableMap.of(
                "account", account
        )), webSocketSession);
    }

    @WebsocketMethodMapping("#inbound.topic == 1003")
    public void testBinaryMessage(WebSocketSession webSocketSession, BinaryMessage binaryMessage) {
        final String hex = ByteUtils.bytes2Hex(binaryMessage.getPayload().array());
        log.info("{} binary", hex);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(TOPIC_1003.topic).ok(ImmutableMap.of(
                "testBinaryMessage", binaryMessage.getPayload()
        )), webSocketSession);
    }

    @WebsocketMethodMapping({
            "#inbound.topic == 1003",
            "#inbound.version > 3"
    })
    public void testBinaryMessage(WebSocketSession webSocketSession, byte[] bytes) {
        final String hex = ByteUtils.bytes2Hex(bytes);
        log.info("{} binary", hex);
        WebsocketMessageEmitter.emit(WsOutboundBean.topic(TOPIC_1003.topic).ok(ImmutableMap.of(
                "testBinaryMessage", bytes
        )), webSocketSession);
    }

    @WebsocketMethodMapping({
            "#inbound.topic == 1004",
            "#inbound.version > 3"
    })
    public WsOutboundBean<?> testNestMessage(ParentVO req) {
        log.info("{} testNestMessage", req);
        return WsOutboundBean.topic(TOPIC_1004.topic).ok(ImmutableMap.of(
                "testNestMessage", req
        ));
    }

    @Data
    public static class ParentVO {

        private String parentName;
        private SubVO sub;

        @Data
        public static class SubVO {
            private String subName;
        }
    }
}
