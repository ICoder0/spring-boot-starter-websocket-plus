package com.icoder0.websocket.spring.utils;

import com.alibaba.fastjson.JSON;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Slf4j
@UtilityClass
public class WebsocketMessageEmitter {

    public void emit(Object data, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(JSON.toJSONString(data)));
        } catch (IOException e) {
            log.error("{} send message {} error", session, data);
        }
    }
}
