package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.io.IOExceptionWithCause;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.json.PinGsonBuilderFactory;

public class PinGsonBuilderFactoryTest {

    private Gson gson = PinGsonBuilderFactory.make().create();

    @Test
    public void whenSerializingThenDoNotEscapeHtml() {
        var user = new UserDTO(1L, "firstName with <>", "lastName");
        var json = gson.toJson(user);
        assertEquals("{\"id\":1,\"firstName\":\"firstName with <>\",\"lastName\":\"lastName\"}", json);
    }

    @Test
    public void whenSerializingThenAcceptEscapedHtml() {
        var json = "{\"id\":1,\"firstName\":\"firstName with \\u003c\\u003e\",\"lastName\":\"lastName\"}";
        var user = gson.fromJson(json, UserDTO.class);
        assertEquals(1L, user.getId());
        assertEquals("firstName with <>", user.getFirstName());
        assertEquals("lastName", user.getLastName());
    }

    @Test
    public void whenSerializingThenSkipNulls() {
        var user = new UserDTO(1L, "firstName with <>", null);
        var json = gson.toJson(user);
        assertEquals("{\"id\":1,\"firstName\":\"firstName with <>\"}", json);
    }

    @Test
    public void whenSerializingLocalDateTimeThenUseISO() {
        var time = new TimeDTO();
        time.setLocalDateTime(LocalDateTime.of(2020, 1, 2, 3, 4, 5));
        var json = gson.toJson(time);
        assertEquals("{\"localDateTime\":\"2020-01-02T03:04:05\"}", json);
    }

    @Test
    public void whenDeSerializingLocalDateTimeThenUseISO() {
        var json = "{\"localDateTime\":\"2020-01-02T03:04:05\"}";
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), time.getLocalDateTime());
    }

    @Test
    public void whenSerializingLocalDateThenUseISO() {
        var time = new TimeDTO();
        time.setLocalDate(LocalDate.of(2020, 1, 2));
        var json = gson.toJson(time);
        assertEquals("{\"localDate\":\"2020-01-02\"}", json);
    }

    @Test
    public void whenDeSerializingLocalDateThenUseISO() {
        var json = "{\"localDate\":\"2020-01-02\"}";
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(LocalDate.of(2020, 1, 2), time.getLocalDate());
    }

    @Test
    public void whenSerializingZonedDateTimeThenUseISOAndZifUTC() {
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = new TimeDTO();
        time.setZonedDateTime(ZonedDateTime.of(localDateTime, zoneId));
        var json = gson.toJson(time);
        assertEquals("{\"zonedDateTime\":\"2020-01-02T03:04:05Z\"}", json);
    }

    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAndZifUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05Z\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(ZonedDateTime.of(localDateTime, zoneId), time.getZonedDateTime());
    }

    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAnd0000ifUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05+00:00\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(ZonedDateTime.of(localDateTime, zoneId), time.getZonedDateTime());
    }

    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAndNothingIfUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05+00:00\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(ZonedDateTime.of(localDateTime, zoneId), time.getZonedDateTime());
    }

    @Test
    public void whenSerializingZonedDateTimeThenUseISO() {
        final ZoneId zoneId = ZoneId.of(ZoneOffset.ofHoursMinutes(-3, 0).getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var user = Map.of("zonedDateTime", ZonedDateTime.of(localDateTime, zoneId));
        var json = gson.toJson(user);
        assertEquals("{\"zonedDateTime\":\"2020-01-02T03:04:05-03:00\"}", json);
    }

    @Test
    public void whenSerializingExceptionsThenOnlyShowTypeAndMessage() {
        Exception e = new IOExceptionWithCause("this is the message", new IllegalArgumentException("this is an internal message"));
        var json = gson.toJson(e);
        assertEquals("{\"type\":\"org.apache.commons.io.IOExceptionWithCause\",\"message\":\"this is the message\"}", json);

    }

    @Test
    public void whenSerializingPinBadRequestExceptionThenShowInternalData() {
        PinBadRequestException e = new PinBadRequestException("id", "zzzzz", "Long.class", new NumberFormatException());
        var json = gson.toJson(e);
        assertEquals(
                "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field id with value zzzzz can not be converted to Long.class\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"id\",\"currentValue\":\"zzzzz\",\"destinationClassName\":\"Long.class\"}",
                json);

    }
}