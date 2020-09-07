package com.icoder0.websocket.example;

import com.google.common.collect.ImmutableMap;
import com.icoder0.websocket.annotation.*;
import com.icoder0.websocket.core.model.WsOutboundBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/9/4
 */
@Slf4j
@WebsocketMapping(value = "/api/mirai", prototype = true)
public class WsSpecificationTest {

    /**
     * 默认入参都从payload中提取, 如果需要提取header字段, 需要声明@WebsocketHeader.
     * 如果业务参数中有字段名与header系统字段冲突, 需要声明@WebsocketPayload, 并声明该字段在payload中的字段名, 否则编译阶段不通过.
     */
    @WebsocketMethodMapping("#inbound.code == 8001")
    public String login2(WebSocketSession session,
                                    @WebsocketHeader(isSequence = true) Long seq,
                                    @WebsocketPayload(value = "sequence") Long nestSeq,
                                    @WebsocketHeader(isFunctionCode = true) Integer code,
                                    Integer ver,
                                    PayloadVO req,
                                    Map<String, Object> reqDup,
                                    @WebsocketPayload("params") Map<String, Object> innerParams
    ) {
        log.info("test#seq {}", seq);
        return "123";
    }

    @Data
    public static class PayloadVO {
        private Integer ver;
        private String params;
    }
}
