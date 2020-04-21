package com.pinframework.converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PinZonedDateTimeParamConverter extends PinAbstractParamConverter<ZonedDateTime> {

    @Override
    protected Class<ZonedDateTime> targetClass() {
        return ZonedDateTime.class;
    }

    @Override
    protected ZonedDateTime convertValue(String paramValue) {
        return ZonedDateTime.parse(paramValue, DateTimeFormatter.ISO_DATE_TIME);
    }
}
