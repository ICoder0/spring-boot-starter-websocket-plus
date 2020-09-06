package com.icoder0.websocket.spring;

import com.icoder0.websocket.core.model.WsInboundBean;
import com.icoder0.websocket.core.model.WsInboundBeanSpecification;
import com.icoder0.websocket.core.model.WsOutboundBean;
import com.icoder0.websocket.core.model.WsOutboundBeanSpecification;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author bofa1ex
 * @since 2020/8/2
 */
@Setter
@ConfigurationProperties(prefix = WebsocketPlusProperties.PREFIX)
public class WebsocketPlusProperties {

    public static final String PREFIX                                             = "websocket-plus";

    public static Long asyncSendTimeout                                           = TimeUnit.SECONDS.toMillis(5);

    public static Long maxSessionIdleTimeout                                      = TimeUnit.MINUTES.toMillis(10);

    public static Integer maxTextMessageBufferSize                                = 10240;

    public static Integer maxBinaryMessageBufferSize                              = 10240;

    /** websocket handler registration properties */
    public static boolean withSockJS                                              = false;
    public static String[] origins                                                = new String[]{"*"};

    /** decode properties */
    public static Class<? extends WsInboundBeanSpecification>  inboundBeanClazz   = WsInboundBean.class;
    public static Class<? extends WsOutboundBeanSpecification> outboundBeanClazz  = WsOutboundBean.class;

    /** specification */
    public static String inboundSpecification                                     = "{seq:0, code:xxx, version:0, params:{}}";
    public static String outboundSpecification                                    = "{seq:0, code:xxx, message:{\"this is message\"}, content:{}}";

    /** inbound decode-field-name */
    public static String payloadParamsDecodeName                                  = "params";
    public static String payloadSequenceDecodeName                                = "sequence";
    public static String payloadFunctionCodeDecodeName                            = "code";

    /** spel properties */
    public static String spelVariableName                                         = "inbound";

    public void setAsyncSendTimeout(Long asyncSendTimeout) {
        WebsocketPlusProperties.asyncSendTimeout = asyncSendTimeout;
    }

    public void setMaxSessionIdleTimeout(Long maxSessionIdleTimeout) {
        WebsocketPlusProperties.maxSessionIdleTimeout = maxSessionIdleTimeout;
    }

    public void setMaxTextMessageBufferSize(Integer maxTextMessageBufferSize) {
        WebsocketPlusProperties.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public void setMaxBinaryMessageBufferSize(Integer maxBinaryMessageBufferSize) {
        WebsocketPlusProperties.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    public void setWithSockJS(boolean withSockJS) {
        WebsocketPlusProperties.withSockJS = withSockJS;
    }

    public void setOrigins(String[] origins) {
        WebsocketPlusProperties.origins = origins;
    }

    public void setInboundBeanClazz(Class<? extends WsInboundBeanSpecification> inboundBeanClazz) {
        WebsocketPlusProperties.inboundBeanClazz = inboundBeanClazz;
    }

    public void setOutboundBeanClazz(Class<? extends WsOutboundBeanSpecification> outboundBeanClazz) {
        WebsocketPlusProperties.outboundBeanClazz = outboundBeanClazz;
    }

    public void setInboundSpecification(String inboundSpecification) {
        WebsocketPlusProperties.inboundSpecification = inboundSpecification;
    }

    public void setOutboundSpecification(String outboundSpecification) {
        WebsocketPlusProperties.outboundSpecification = outboundSpecification;
    }

    public void setPayloadParamsDecodeName(String payloadParamsDecodeName) {
        WebsocketPlusProperties.payloadParamsDecodeName = payloadParamsDecodeName;
    }

    public void setPayloadSequenceDecodeName(String payloadSequenceDecodeName) {
        WebsocketPlusProperties.payloadSequenceDecodeName = payloadSequenceDecodeName;
    }

    public void setPayloadFunctionCodeDecodeName(String payloadFunctionCodeDecodeName) {
        WebsocketPlusProperties.payloadFunctionCodeDecodeName = payloadFunctionCodeDecodeName;
    }

    public void setSpelVariableName(String spelVariableName) {
        WebsocketPlusProperties.spelVariableName = spelVariableName;
    }
}
