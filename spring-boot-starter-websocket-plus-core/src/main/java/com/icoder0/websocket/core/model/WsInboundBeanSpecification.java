package com.icoder0.websocket.core.model;



/**
 * @author bofa1ex
 * @since 2020/9/4
 */
public interface WsInboundBeanSpecification {

    Long sequence();

    String topic();

    String params();
}
