package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
public class WsException extends RuntimeException {

    private WsBusiCode wsBusiCode;

    public WsException(WsBusiCode wsBusiCode) {
        this.wsBusiCode = wsBusiCode;
    }

    public WsException(String message, WsBusiCode wsBusiCode) {
        super(message);
        this.wsBusiCode = wsBusiCode;
    }

    public WsException(String message, Throwable cause, WsBusiCode wsBusiCode) {
        super(message, cause);
        this.wsBusiCode = wsBusiCode;
    }
}
