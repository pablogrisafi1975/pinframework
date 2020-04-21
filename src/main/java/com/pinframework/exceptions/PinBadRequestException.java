package com.pinframework.exceptions;

import java.util.StringJoiner;

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

    public PinBadRequestException(String fieldName, String currentValue, Class<?> destinationClass, Throwable cause) {
        super("The field " + fieldName + " with value " + currentValue + " can not be converted to " + createDestinationDescription(
                destinationClass), cause);
        this.messageKey = MESSAGE_KEY_CAN_NOT_CONVERT;
        this.fieldName = fieldName;
        this.currentValue = currentValue;
        this.destinationClassName = destinationClass.getSimpleName();
    }

    private static String createDestinationDescription(Class<?> destinationClass) {
        if (destinationClass.isEnum()) {
            StringJoiner joiner = new StringJoiner(",");
            for (Object o : destinationClass.getEnumConstants()) {
                Enum<?> enumClass = (Enum<?>) o;
                String name = enumClass.name();
                joiner.add(name);
            }
            return destinationClass.getSimpleName() + "[" + joiner.toString() + "]";
        }
        return destinationClass.getSimpleName();
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
