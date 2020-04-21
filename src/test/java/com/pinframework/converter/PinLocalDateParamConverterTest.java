package com.pinframework.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.pinframework.exceptions.PinBadRequestException;

public class PinLocalDateParamConverterTest {
    private PinLocalDateParamConverter converter = new PinLocalDateParamConverter();

    @Test
    void whenConvertValidThenReturnConverted() {
        LocalDate result = converter.convert("paramName", "2020-03-07");
        assertEquals(LocalDate.of(2020,3,7), result);
    }

    @Test
    void whenConvertInvalidThenThrowPinBadRequestException() {
        PinBadRequestException thrown = assertThrows(PinBadRequestException.class,
                () -> converter.convert("paramName", "abcdef"));
        assertEquals(thrown.getMessageKey(), "CAN_NOT_CONVERT");
        assertEquals(thrown.getDestinationClassName(), "LocalDate");
        assertEquals(thrown.getCurrentValue(), "abcdef");
        assertEquals(thrown.getFieldName(), "paramName");
        assertEquals(thrown.getMessage(), "The field paramName with value abcdef can not be converted to LocalDate");
    }
}
