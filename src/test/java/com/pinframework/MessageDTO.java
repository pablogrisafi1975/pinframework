package com.pinframework;

public class MessageDTO {
    private final String type;
    private final String message;

    public MessageDTO(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
