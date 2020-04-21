package com.pinframework.converter;

import com.pinframework.exceptions.PinBadRequestException;

@FunctionalInterface
public interface PinParamConverter<T> {
    T convert(String paramName, String paramValue) throws PinBadRequestException;
}
