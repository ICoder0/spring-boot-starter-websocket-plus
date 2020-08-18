package com.icoder0.websocket.core.annotation;

import com.icoder0.websocket.core.aop.WebsocketMessageAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/8/14
 */
@Configuration
public class WebsocketConfigurationSupportEx extends WebSocketConfigurationSupport {

    @Autowired
    private WebsocketPlusProperties websocketPlusProperties;

    @Autowired
    private WebsocketPlusMetadataProperties websocketPlusMetadataProperties;

    @Autowired(required = false)
    private List<HandshakeInterceptor> handshakeInterceptors;

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final List<WebSocketConfigurer> configurers = websocketPlusMetadataProperties.getMappingHandlerMethodMetadataMap().entrySet().parallelStream()
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
