package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.spring.WebsocketPlusProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
public class WebsocketRegistrySupport extends WebSocketConfigurationSupport {

    @Autowired
    private WebsocketPlusProperties websocketPlusProperties;

    @Autowired
    private WebsocketProcessorAttributes websocketProcessorAttributes;

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final List<WebSocketConfigurer> configurers = websocketProcessorAttributes.getMappingHandlerMethodMetadataMap().entrySet().parallelStream()
                .map(entry -> (WebSocketConfigurer) _registry -> {
                    final WebSocketHandlerRegistration webSocketHandlerRegistration = _registry.addHandler(entry.getValue().getArchetypeHandler(), entry.getKey());
                    if (websocketPlusProperties.isWithSockJS()) {
                        webSocketHandlerRegistration.withSockJS();
                    }
                    Optional.ofNullable(websocketPlusProperties.getOrigins()).ifPresent(webSocketHandlerRegistration::setAllowedOrigins);
                })
                .collect(Collectors.toList());
        for (WebSocketConfigurer configurer : configurers) {
            configurer.registerWebSocketHandlers(registry);
        }
    }

}
