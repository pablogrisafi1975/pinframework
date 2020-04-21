package com.pinframework.converter;

public class PinLongParamConverter extends PinAbstractParamConverter<Long> {

    @Override
    protected Class<Long> targetClass() {
        return Long.class;
    }

    @Override
    protected Long convertValue(String paramValue) {
        return Long.parseLong(paramValue);
    }
}
