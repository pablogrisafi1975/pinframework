package com.pinframework.exceptions;

public class PinBadRequestException extends PinRuntimeException {

    private static final String MESSAGE_KEY_CAN_NOT_CONVERT = "CAN_NOT_CONVERT";
    private static final String MESSAGE_KEY_CAN_NOT_PARSE = "CAN_NOT_PARSE";

    private final String messageKey;
    private final String fieldName;
    private final String currentValue;
    private final String destinationClassName;

    public PinBadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = MESSAGE_KEY_CAN_NOT_PARSE;
        this.fieldName = null;
        this.currentValue = null;
        this.destinationClassName = null;
    }

    public PinBadRequestException(String fieldName, String currentValue, String destinationClassName, Throwable cause) {
        super("The field " + fieldName + " with value " + currentValue + " can not be converted to " + destinationClassName, cause);
        this.messageKey = MESSAGE_KEY_CAN_NOT_CONVERT;
        this.fieldName = fieldName;
        this.currentValue = currentValue;
        this.destinationClassName = destinationClassName;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public String getDestinationClassName() {
        return destinationClassName;
    }
}
