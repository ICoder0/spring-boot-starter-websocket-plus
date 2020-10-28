package com.icoder0.websocket.example;

import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.annotation.WebsocketMapping;
import com.icoder0.websocket.annotation.WebsocketMethodMapping;
import com.icoder0.websocket.core.model.WsOutboundBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotNull;


/**
 * @author bofa1ex
 * @since 2020/7/31
 */
@Slf4j
@WebsocketMapping(value = "/api/mirai", prototype = true)
public class WsBootStrapDup {

    @WebsocketMethodMapping("#inbound.topic == 1006")
    public WsOutboundBean<?> subTest(@NotNull Long account) {
        log.info("subTest account#{}", account);
        return WsOutboundBean.ok().body(ImmutableMap.of(
                "hello", "world"
        ));
    }

}
