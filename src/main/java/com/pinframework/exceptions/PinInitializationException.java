package com.pinframework.exceptions;

public class PinInitializationException extends PinRuntimeException {

    public PinInitializationException(Throwable cause) {
        super(cause);
    }

    public PinInitializationException(String message) {
        super(message);
    }

    public PinInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
