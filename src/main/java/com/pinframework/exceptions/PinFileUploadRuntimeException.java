package com.pinframework.exceptions;

import org.apache.commons.fileupload.FileUploadException;

public class PinFileUploadRuntimeException extends PinRuntimeException {
    public PinFileUploadRuntimeException(FileUploadException cause) {
        super(cause);
    }
}
