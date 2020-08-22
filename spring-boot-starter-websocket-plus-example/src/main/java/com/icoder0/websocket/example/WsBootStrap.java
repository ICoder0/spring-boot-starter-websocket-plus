package com.icoder0.websocket.example;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.core.model.WsOutboundBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


/**
 * @author bofa1ex
 * @since 2020/7/31
 */
@Slf4j
@WebsocketMapping("/api/mirai")
public class WsBootStrap {

    @WebsocketMethodMapping("#inbound.code == 1001")
    public WsOutboundBean login(WebSocketSession webSocketSession, @Validated WsLoginVO req) {
        log.info("login {}", req);
        webSocketSession.getAttributes().put("account", req.getAccount());
        return WsOutboundBean.ok()
                .body(ImmutableMap.of(
                        "hello", "world"
                ));
    }

    @WebsocketMethodMapping("#inbound.code == 1003")
    public void logout(WebSocketSession webSocketSession) throws IOException {
        final Long account = (Long) webSocketSession.getAttributes().get("account");
        log.info("{} logout", account);
        webSocketSession.sendMessage(new TextMessage(
                JSON.toJSONString(ImmutableMap.of(
                        "account", account
                ))
        ));
    }
}
