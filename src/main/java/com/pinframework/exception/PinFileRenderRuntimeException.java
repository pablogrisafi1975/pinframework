package com.pinframework.exception;

public class PinFileRenderRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PinFileRenderRuntimeException(String message, Exception cause) {
    super(message, cause);
  }

  public PinFileRenderRuntimeException(String message) {
    super(message);
  }
}
