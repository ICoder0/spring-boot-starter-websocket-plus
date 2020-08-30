package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/8/30
 */
public class WsRequestParamException extends WsException {

    public WsRequestParamException(String message) {
        this(message, null);
    }

    public WsRequestParamException(String message, Throwable cause) {
        super(WsBusiCode.ILLEGAL_REQUEST_ERROR, "#REQUEST_PARAM#" + "\n" +
                message, cause);
    }
}
