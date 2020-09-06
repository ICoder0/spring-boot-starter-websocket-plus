package com.icoder0.websocket.core.exception;

import com.icoder0.websocket.core.model.WsBusiCode;

/**
 * @author bofa1ex
 * @since 2020/8/30
 */
public class WsSpecificationException extends WsException {

    public WsSpecificationException(String message) {
        super(WsBusiCode.ILLEGAL_REQUEST_ERROR, message);
    }
}
