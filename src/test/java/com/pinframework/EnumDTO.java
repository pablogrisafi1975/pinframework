package com.pinframework;

import java.time.Month;

public class EnumDTO {
    private final Month month;

    public EnumDTO(Month month) {
        this.month = month;
    }

    public Month getMonth() {
        return month;
    }
}
