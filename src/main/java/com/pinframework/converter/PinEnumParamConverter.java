package com.pinframework.converter;

import com.pinframework.exceptions.PinBadRequestException;

public class PinEnumParamConverter<E extends Enum<E>> implements PinParamConverter<E> {

    public PinEnumParamConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    private final Class<E> enumClass;

    @Override
    public E convert(String paramName, String paramValue) throws PinBadRequestException {
        E value;
        try {
            value = Enum.valueOf(enumClass, paramValue);
        } catch (Exception ex) {
            throw new PinBadRequestException(paramName, paramValue, enumClass, ex);
        }
        return value;
    }

}
