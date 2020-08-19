package com.icoder0.websocket.spring.handler.model;

import com.icoder0.websocket.spring.WebsocketArchetypeHandler;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/8/18
 */
@Builder
@Getter
public class WsMappingHandlerMetadata {
    private final List<WsMappingHandlerMethodMetadata> wsMappingHandlerMethodMetadatas;
    private final WebsocketArchetypeHandler archetypeHandler;
}
