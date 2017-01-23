package com.pinframework.exception;

public class PinInitializationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

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
