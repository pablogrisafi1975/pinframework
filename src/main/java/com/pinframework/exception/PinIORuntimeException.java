package com.pinframework.exception;

import java.io.IOException;

public class PinIORuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PinIORuntimeException(IOException cause) {
		super(cause);
	}

}
