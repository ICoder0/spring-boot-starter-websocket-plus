package com.icoder0.websocket.spring.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedList;
import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/9/6
 */
@Slf4j
public class WebsocketMessageCustomizerRegistry {

    private final List<WebsocketMessageCustomizer> customizers = new LinkedList<>();

    @Autowired(required = false)
    public void setConfigurers(List<WebsocketMessageCustomizer> customizers) {
        if (!CollectionUtils.isEmpty(customizers)) {
            this.customizers.addAll(customizers);
        }
    }

    public void handleWebsocketMessageCustomizer(WebSocketSession session, WebSocketMessage<?> message){
        for (WebsocketMessageCustomizer customizer : customizers) {
            customizer.customize(session, message);
        }
    }
}
