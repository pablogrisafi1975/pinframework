package com.pinframework;

import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.pinframework.json.PinGsonBuilderFactory;

public class PinGsonBuilderFactoryTest {

    private Gson gson;

    @BeforeMethod
    public void setUp() {
        gson = PinGsonBuilderFactory.make().create();
    }

    @Test
    public void whenSerializingThenDoNotEscapeHtml() {
        var user = new UserDTO(1L, "firstName with <>", "lastName");
        var json = gson.toJson(user);
        assertEquals(json, "{\"id\":1,\"firstName\":\"firstName with <>\",\"lastName\":\"lastName\"}");
    }

    @Test
    public void whenSerializingThenAcceptEscapedHtml() {
        var json = "{\"id\":1,\"firstName\":\"firstName with \\u003c\\u003e\",\"lastName\":\"lastName\"}";
        var user = gson.fromJson(json, UserDTO.class);
        assertEquals(user.getId(), Long.valueOf(1));
        assertEquals(user.getFirstName(), "firstName with <>");
        assertEquals(user.getLastName(), "lastName");
    }

    @Test
    public void whenSerializingThenSkipNulls() {
        var user = new UserDTO(1L, "firstName with <>", null);
        var json = gson.toJson(user);
        assertEquals(json, "{\"id\":1,\"firstName\":\"firstName with <>\"}");
    }

    @Test
    public void whenSerializingLocalDateTimeThenUseISO() {
        var time = new TimeDTO();
        time.setLocalDateTime(LocalDateTime.of(2020, 1, 2, 3, 4, 5));
        var json = gson.toJson(time);
        assertEquals(json, "{\"localDateTime\":\"2020-01-02T03:04:05\"}");
    }

    @Test
    public void whenDeSerializingLocalDateTimeThenUseISO() {
        var json = "{\"localDateTime\":\"2020-01-02T03:04:05\"}";
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(time.getLocalDateTime(), LocalDateTime.of(2020, 1, 2, 3, 4, 5));
    }

    @Test
    public void whenSerializingLocalDateThenUseISO() {
        var time = new TimeDTO();
        time.setLocalDate(LocalDate.of(2020, 1, 2));
        var json = gson.toJson(time);
        assertEquals(json, "{\"localDate\":\"2020-01-02\"}");
    }

    @Test
    public void whenDeSerializingLocalDateThenUseISO() {
        var json = "{\"localDate\":\"2020-01-02\"}";
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(time.getLocalDate(), LocalDate.of(2020, 1, 2));
    }

    @Test
    public void whenSerializingZonedDateTimeThenUseISOAndZifUTC() {
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = new TimeDTO();
        time.setZonedDateTime(ZonedDateTime.of(localDateTime, zoneId));
        var json = gson.toJson(time);
        assertEquals(json, "{\"zonedDateTime\":\"2020-01-02T03:04:05Z\"}");
    }
    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAndZifUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05Z\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(time.getZonedDateTime(), ZonedDateTime.of(localDateTime, zoneId));
    }

    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAnd0000ifUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05+00:00\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(time.getZonedDateTime(), ZonedDateTime.of(localDateTime, zoneId));
    }

    @Test
    public void whenDeSerializingZonedDateTimeThenUseISOAndNothingIfUTC() {
        var json = "{\"zonedDateTime\":\"2020-01-02T03:04:05+00:00\"}";
        final ZoneId zoneId = ZoneId.of(ZoneOffset.UTC.getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var time = gson.fromJson(json, TimeDTO.class);
        assertEquals(time.getZonedDateTime(), ZonedDateTime.of(localDateTime, zoneId));
    }


    @Test
    public void whenSerializingZonedDateTimeThenUseISO() {
        final ZoneId zoneId = ZoneId.of(ZoneOffset.ofHoursMinutes(-3, 0).getId());
        LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 2, 3, 4, 5);
        var user = Map.of("zonedDateTime", ZonedDateTime.of(localDateTime, zoneId));
        var json = gson.toJson(user);
        assertEquals(json, "{\"zonedDateTime\":\"2020-01-02T03:04:05-03:00\"}");
    }
}