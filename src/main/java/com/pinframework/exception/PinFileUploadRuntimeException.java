package com.pinframework.exception;

import org.apache.commons.fileupload.FileUploadException;

public class PinFileUploadRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PinFileUploadRuntimeException(FileUploadException cause) {
		super(cause);
	}
}
