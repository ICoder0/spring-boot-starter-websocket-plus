package com.icoder0.websocket.core.exception;

import lombok.Getter;

/**
 * @author bofa1ex
 * @since 2020/8/20
 */
@Getter
public enum WsBusiCode {
    OK("200", "[%s]", ""),

    ILLEGAL_REQUEST_ERROR("400", "检查请求资源规范 [%s]", "check your request message specification [%s]"),

    AUTH_ERROR("403", "检查认证信息 [%s]", "check your auth arguments [%s]"),

    INTERNAL_ERROR("500", "系统内部异常 [%s]", "system internal exception [%s]"),

    BUSINESS_ERROR("600", "业务数据异常 [%s]", "business data exception [%s]");

    WsBusiCode(String code, String zhMsg, String enMsg) {
        this.code = code;
        this.zhMsg = zhMsg;
        this.enMsg = enMsg;
    }

    private final String code;
    private final String zhMsg;
    private final String enMsg;
}
