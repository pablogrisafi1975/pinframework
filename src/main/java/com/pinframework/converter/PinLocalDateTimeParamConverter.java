package com.pinframework.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PinLocalDateTimeParamConverter extends PinAbstractParamConverter<LocalDateTime> {

    @Override
    protected Class<LocalDateTime> targetClass() {
        return LocalDateTime.class;
    }

    @Override
    protected LocalDateTime convertValue(String paramValue) {
        return LocalDateTime.parse(paramValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
