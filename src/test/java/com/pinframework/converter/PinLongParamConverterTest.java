package com.pinframework.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.pinframework.exceptions.PinBadRequestException;

public class PinLongParamConverterTest {
    private PinLongParamConverter converter = new PinLongParamConverter();

    @Test
    void whenConvertValidThenReturnConverted() {
        Long result = converter.convert("paramName", "12345");
        assertEquals(Long.valueOf(12345), result);
    }

    @Test
    void whenConvertInvalidThenThrowPinBadRequestException() {
        PinBadRequestException thrown = assertThrows(PinBadRequestException.class,
                () -> converter.convert("paramName", "abcdef"));
        assertEquals(thrown.getMessageKey(), "CAN_NOT_CONVERT");
        assertEquals(thrown.getDestinationClassName(), "Long");
        assertEquals(thrown.getCurrentValue(), "abcdef");
        assertEquals(thrown.getFieldName(), "paramName");
        assertEquals(thrown.getMessage(), "The field paramName with value abcdef can not be converted to Long");
    }
}
