package com.icoder0.websocket.core.exception;

import com.icoder0.websocket.core.model.WsBusiCode;
import lombok.Getter;

/**
 * @author bofa1ex
 * @since 2020/8/22
 */

public class WsException extends RuntimeException {
    @Getter
    private final WsBusiCode wsBusiCode;

    public WsException(WsBusiCode wsBusiCode) {
        this.wsBusiCode = wsBusiCode;
    }

    public WsException(WsBusiCode wsBusiCode, String message) {
        super(message);
        this.wsBusiCode = wsBusiCode;
    }
}
