package com.pinframework.converter;

import com.pinframework.exceptions.PinBadRequestException;

public abstract class PinAbstractParamConverter<T> implements PinParamConverter<T> {

    @Override
    public T convert(String paramName, String paramValue) throws PinBadRequestException {
        if(paramValue.length() == 0){
            return null;
        }
        T value;
        try {
            value = convertValue(paramValue);
        } catch (Exception ex) {
            throw new PinBadRequestException(paramName, paramValue, targetClass(), ex);
        }
        return value;
    }

    protected abstract Class<T> targetClass();

    protected abstract T convertValue(String paramValue);
}
