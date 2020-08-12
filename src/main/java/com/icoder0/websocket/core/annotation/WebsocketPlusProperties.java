package com.icoder0.websocket.core.annotation;

import com.icoder0.websocket.core.model.WsInboundBean;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
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

    /** websocket handler registration properties */
    private boolean withSockJS = false;
    private String[] origins = new String[]{"*"};

    /** decode properties */
    private Class<?> outerDecodeClazz = WsInboundBean.class;
    private String innerDecodeParamKeyName = "params";

    /** spel properties */
    private String spelRootName = "inbound";
}
