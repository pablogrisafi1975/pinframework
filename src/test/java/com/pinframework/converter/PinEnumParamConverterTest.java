package com.pinframework.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Month;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.pinframework.exceptions.PinBadRequestException;

class PinEnumParamConverterTest {
    PinEnumParamConverter<Month> converter = new PinEnumParamConverter(Month.class);

    @Test
    void whenConvertValidThenReturnConverted() {
        Month result = converter.convert("paramName", "APRIL");
        Assertions.assertEquals(Month.APRIL, result);
    }

    @Test
    void whenConvertInvalidThenThrowPinBadRequestException() {
        PinBadRequestException thrown = assertThrows(PinBadRequestException.class,
                () -> converter.convert("paramName", "abcdef"));
        assertEquals(thrown.getMessageKey(), "CAN_NOT_CONVERT");
        assertEquals(thrown.getDestinationClassName(), "Month");
        assertEquals(thrown.getCurrentValue(), "abcdef");
        assertEquals(thrown.getFieldName(), "paramName");
        assertEquals(thrown.getMessage(), "The field paramName with value abcdef can not be converted to Month[JANUARY,FEBRUARY,MARCH,APRIL,MAY,JUNE,JULY,AUGUST,SEPTEMBER,OCTOBER,NOVEMBER,DECEMBER]");
    }
}
