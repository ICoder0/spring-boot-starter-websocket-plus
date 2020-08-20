package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/8/20
 */
public enum WsBusiCode {
    REQUEST_ERROR("400", "检查请求资源规范", "check your request message specification");

    WsBusiCode(String code, String zhMsg, String enMsg) {
        this.code = code;
        this.zhMsg = zhMsg;
        this.enMsg = enMsg;
    }

    private String code;
    private String zhMsg;
    private String enMsg;
}
