package com.icoder0.websocket.core.constant;


/**
 * @author bofa1ex
 * @since 2020/11/18
 */
public enum WsNativeTopic {
    NO_TOPIC(-1L, "Topic Not Found");

    WsNativeTopic(Long topic, String desc) {
        this.topic = topic;
        this.desc = desc;
    }

    public final Long topic;
    public final String desc;
}
