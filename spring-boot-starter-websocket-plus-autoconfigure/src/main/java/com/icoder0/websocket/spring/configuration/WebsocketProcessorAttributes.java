package com.icoder0.websocket.spring.configuration;

import com.icoder0.websocket.spring.handler.model.WsMappingHandlerMetadata;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/8/19
 */
@Data
public class WebsocketProcessorAttributes {
    private final Map<String, WsMappingHandlerMetadata> mappingHandlerMethodMetadataMap = new HashMap<>(2 >> 6);
}
