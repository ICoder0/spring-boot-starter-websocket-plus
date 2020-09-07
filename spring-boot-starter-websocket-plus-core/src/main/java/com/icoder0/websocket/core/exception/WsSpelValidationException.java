package com.icoder0.websocket.core.exception;

import com.icoder0.websocket.core.constant.WsBusiCode;

/**
 * @author bofa1ex
 * @since 2020/8/12
 */
public class WsSpelValidationException extends WsException {

    public WsSpelValidationException() {
        super(WsBusiCode.ILLEGAL_REQUEST_ERROR);
    }
}
