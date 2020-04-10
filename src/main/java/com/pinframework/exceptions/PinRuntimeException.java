package com.pinframework.exceptions;

public class PinRuntimeException extends RuntimeException {

    public PinRuntimeException() {
    }

    public PinRuntimeException(String message) {
        super(message);
    }

    public PinRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PinRuntimeException(Throwable cause) {
        super(cause);
    }

    public PinRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
