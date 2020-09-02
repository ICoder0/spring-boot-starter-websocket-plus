package com.icoder0.websocket.core.exception;

/**
 * @author bofa1ex
 * @since 2020/9/2
 */
public class WsMappingPrototypeException extends WsException {

    public WsMappingPrototypeException(String message) {
        this(message, null);
    }

    public WsMappingPrototypeException(String message, Throwable cause) {
        super(WsBusiCode.ILLEGAL_REQUEST_ERROR, "#MAPPING_PROTOTYPE_NOT_SUPPORTED#" + "\n" +
                message, cause);
    }
}
