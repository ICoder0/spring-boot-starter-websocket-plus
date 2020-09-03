package com.icoder0.websocket.spring.model;

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
    private final String beanName;
    private final WebsocketArchetypeHandler archetypeHandler;
}
