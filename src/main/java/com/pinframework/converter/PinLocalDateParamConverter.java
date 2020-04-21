package com.pinframework.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PinLocalDateParamConverter extends PinAbstractParamConverter<LocalDate> {

    @Override
    protected Class<LocalDate> targetClass() {
        return LocalDate.class;
    }

    @Override
    protected LocalDate convertValue(String paramValue) {
        return LocalDate.parse(paramValue, DateTimeFormatter.ISO_DATE);
    }
}
