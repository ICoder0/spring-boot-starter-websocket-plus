package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/9/2
 */
public class WsExpressionException extends WsException {

    public WsExpressionException(String message) {
        this(message, null);
    }

    public WsExpressionException(String message, Throwable cause) {
        super(WsBusiCode.ILLEGAL_REQUEST_ERROR, "#EXPRESSION#" + "\n" +
                message, cause);
    }
}
