package com.icoder0.websocket.core.model;

/**
 * @author bofa1ex
 * @since 2020/9/4
 */
public interface WsOutboundBeanSpecification {

    Long sequence();

    void setSequence(Long sequence);

    String topic();

    void setTopic(String topic);
}
