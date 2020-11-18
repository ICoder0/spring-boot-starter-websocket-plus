package com.icoder0.websocket.example;


/**
 * @author bofa1ex
 * @since 2020/11/18
 */
public enum WsExampleTopic {
    TOPIC_1001(1001L, "example login"),
    TOPIC_1002(1002L, "example logout"),
    TOPIC_1003(1003L, "test binary message"),
    TOPIC_1004(1004L, "test nest message"),
    TOPIC_1005(1005L, "test duplicate message");

    public final Long topic;
    public final String desc;

    WsExampleTopic(Long topic, String desc) {
        this.topic = topic;
        this.desc = desc;
    }
}
