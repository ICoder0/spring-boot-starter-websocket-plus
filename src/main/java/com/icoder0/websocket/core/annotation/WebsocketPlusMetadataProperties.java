package com.icoder0.websocket.core.annotation;

import com.icoder0.websocket.core.handler.model.WsExceptionHandlerMethodMetadata;
import com.icoder0.websocket.core.handler.model.WsMappingHandlerMetadata;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/8/17
 */
@Data
@Component
public class WebsocketPlusMetadataProperties {
    private Map<String[], WsMappingHandlerMetadata> mappingHandlerMethodMetadataMap = new HashMap<>(2 >> 6);
    private Map<String[], List<WsExceptionHandlerMethodMetadata>> exceptionMethodMetadataMap = new HashMap<>(2 >> 6);
}
