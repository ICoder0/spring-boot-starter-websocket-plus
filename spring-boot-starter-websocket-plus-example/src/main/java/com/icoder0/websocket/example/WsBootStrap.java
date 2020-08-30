package com.icoder0.websocket.example;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.core.model.WsOutboundBean;
import com.icoder0.websocket.spring.utils.WebsocketMessageEmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
    public void logout(WebSocketSession webSocketSession, @NotBlank @Size(min = 4, max = 6) String account) {
        log.info("{} logout", account);
        WebsocketMessageEmitter.emit(WsOutboundBean.ok(ImmutableMap.of(
                "account", account
        )), webSocketSession);
    }
}
