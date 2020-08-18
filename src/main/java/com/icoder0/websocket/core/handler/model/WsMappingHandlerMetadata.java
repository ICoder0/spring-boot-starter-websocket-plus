package com.icoder0.websocket.core.handler.model;

import com.icoder0.websocket.core.annotation.WebsocketArchetypeHandler;
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
