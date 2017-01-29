package com.pinframework.exception;

import java.io.IOException;

public class PinIoRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PinIoRuntimeException(IOException cause) {
    super(cause);
  }

}
