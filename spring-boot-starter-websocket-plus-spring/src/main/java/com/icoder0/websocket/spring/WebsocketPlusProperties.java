package com.icoder0.websocket.spring;

import com.icoder0.websocket.core.model.WsInboundBean;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author bofa1ex
 * @since 2020/8/2
 */
@Data
@ConfigurationProperties(prefix = WebsocketPlusProperties.PREFIX)
public class WebsocketPlusProperties {

    public static final String PREFIX = "websocket-plus";

    private Long asyncSendTimeout = TimeUnit.SECONDS.toMillis(5);

    private Long maxSessionIdleTimeout = TimeUnit.MINUTES.toMillis(10);

    private Integer maxTextMessageBufferSize = 10240;

    private Integer maxBinaryMessageBufferSize = 10240;

    /**
     * websocket handler registration properties
     */
    private boolean withSockJS = false;
    private String[] origins = new String[]{"*"};

    /**
     * decode properties
     */
    private Class<?> payloadDecodeClazz = WsInboundBean.class;
    private String payloadSpecification = "{seq:0, code:1000, version:0, params:{}}";
    private String payloadParamSpecification = "{..., params:{}}";
    private String payloadParamDecodeName = "params";
    private String payloadSequenceDecodeName = "sequence";
    /**
     * spel properties
     */
    private String spelRootName = "inbound";
}
