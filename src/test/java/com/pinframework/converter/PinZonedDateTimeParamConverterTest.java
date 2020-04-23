package com.pinframework.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.pinframework.exceptions.PinBadRequestException;

public class PinZonedDateTimeParamConverterTest {
    private PinZonedDateTimeParamConverter converter = new PinZonedDateTimeParamConverter();

    @ParameterizedTest
    @MethodSource
    public void whenConvertValidThenReturnConverted(String paramValue, ZonedDateTime expected) {
        ZonedDateTime result = converter.convert("paramName", paramValue);
        assertEquals(expected, result);
    }

    static Stream<Arguments> whenConvertValidThenReturnConverted() {
        return Stream.of(
                arguments("", null),
                arguments("2020-03-07T11:22:33Z", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 11, 22, 33), ZoneOffset.UTC)),
                arguments("2020-03-07T11:22:33+00:00", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 11, 22, 33), ZoneOffset.UTC)),
                arguments("2020-03-07T11:22:33+03:00", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 11, 22, 33), ZoneOffset.ofHoursMinutes(3,0))),
                arguments("2020-03-07T13:22:33.5+03:00", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 13, 22, 33, 500000000), ZoneOffset.ofHoursMinutes(3,0))),
                arguments("2020-03-07T13:22:33.123456789-03:00", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 13, 22, 33, 123456789), ZoneOffset.ofHoursMinutes(-3,0))),
                arguments("2020-03-07T00:00:00+05:30", ZonedDateTime.of(LocalDateTime.of(2020, 3, 7, 0, 0, 0), ZoneOffset.ofHoursMinutes(5,30)))
        );
    }

    @Test
    void whenConvertInvalidThenThrowPinBadRequestException() {
        PinBadRequestException thrown = assertThrows(PinBadRequestException.class,
                () -> converter.convert("paramName", "abcdef"));
        assertEquals(thrown.getMessageKey(), "CAN_NOT_CONVERT");
        assertEquals(thrown.getDestinationClassName(), "ZonedDateTime");
        assertEquals(thrown.getCurrentValue(), "abcdef");
        assertEquals(thrown.getFieldName(), "paramName");
        assertEquals(thrown.getMessage(), "The field paramName with value abcdef can not be converted to ZonedDateTime");
    }
}
