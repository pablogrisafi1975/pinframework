package com.pinframework.exception;

import java.io.UnsupportedEncodingException;

public class PinUnsupportedEncodingRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PinUnsupportedEncodingRuntimeException(UnsupportedEncodingException cause) {
		super(cause);
	}

}
