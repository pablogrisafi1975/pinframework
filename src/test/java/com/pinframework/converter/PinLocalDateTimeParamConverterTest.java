package com.pinframework.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.pinframework.exceptions.PinBadRequestException;

public class PinLocalDateTimeParamConverterTest {
    private PinLocalDateTimeParamConverter converter = new PinLocalDateTimeParamConverter();

    @ParameterizedTest
    @MethodSource
    public void whenConvertValidThenReturnConverted(String paramValue, LocalDateTime expected) {
        LocalDateTime result = converter.convert("paramName", paramValue);
        assertEquals(expected, result);
    }

    static Stream<Arguments> whenConvertValidReturnConverted() {
        return Stream.of(
                arguments("2020-03-07T11:22:33", LocalDateTime.of(2020, 3, 7, 11, 22, 33)),
                arguments("2020-03-07T13:22:33.5", LocalDateTime.of(2020, 3, 7, 13, 22, 33, 500000000)),
                arguments("2020-03-07T13:22:33.123456789", LocalDateTime.of(2020, 3, 7, 13, 22, 33, 123456789)),
                arguments("2020-03-07T00:00:00", LocalDateTime.of(2020, 3, 7, 0, 0, 0))
        );
    }

    @Test
    void whenConvertInvalidThenThrowPinBadRequestException() {
        PinBadRequestException thrown = assertThrows(PinBadRequestException.class,
                () -> converter.convert("paramName", "abcdef"));
        assertEquals(thrown.getMessageKey(), "CAN_NOT_CONVERT");
        assertEquals(thrown.getDestinationClassName(), "LocalDateTime");
        assertEquals(thrown.getCurrentValue(), "abcdef");
        assertEquals(thrown.getFieldName(), "paramName");
        assertEquals(thrown.getMessage(), "The field paramName with value abcdef can not be converted to LocalDateTime");
    }
}
