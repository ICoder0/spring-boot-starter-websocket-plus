package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/8/12
 */
public class WsSpelValidationException extends RuntimeException {


    public WsSpelValidationException() {
    }

    public WsSpelValidationException(String message) {
        super(message);
    }

    public WsSpelValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
