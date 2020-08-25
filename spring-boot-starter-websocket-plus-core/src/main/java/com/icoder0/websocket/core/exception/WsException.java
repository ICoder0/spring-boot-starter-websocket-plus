package com.icoder0.websocket.core.exception;

import lombok.Getter;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */
@Getter
public class WsException extends RuntimeException {

    private WsBusiCode wsBusiCode;

    public WsException(WsBusiCode wsBusiCode) {
        this.wsBusiCode = wsBusiCode;
    }

    public WsException(WsBusiCode wsBusiCode, String message) {
        super(message);
        this.wsBusiCode = wsBusiCode;
    }

    public WsException(WsBusiCode wsBusiCode, String message, Throwable cause) {
        super(message, cause);
        this.wsBusiCode = wsBusiCode;
    }
}
