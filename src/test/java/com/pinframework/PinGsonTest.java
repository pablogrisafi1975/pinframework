package com.pinframework;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

@Test
public class PinGsonTest {
  private Gson pinGson = PinGson.getInstance();

  @Test
  public void serializeLocalDateTime() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setLocalDateTime(LocalDateTime.of(2017, 1, 2, 3, 4, 5));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"localDateTime\":\"2017-01-02T03:04:05\"}");
  }

  @Test
  public void serializeZonedDateTime() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setZonedDateTime(ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.ofHours(3)));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"zonedDateTime\":\"2017-01-02T03:04:05+03:00\"}");
  }

  @Test
  public void serializeZonedDateTimeUtc() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setZonedDateTime(ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"zonedDateTime\":\"2017-01-02T03:04:05Z\"}");
  }

  @Test
  public void serializeLocalDate() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setLocalDate(LocalDate.of(2017, 1, 2));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"localDate\":\"2017-01-02\"}");
  }

  @Test
  public void deserializeLocalDateTimeProper() {
    String json = "{\"localDateTime\":\"2017-01-02T03:04:05\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getLocalDateTime(), LocalDateTime.of(2017, 1, 2, 3, 4, 5));
  }

  @Test
  public void deserializeLocalDateTimeZ() {
    String json = "{\"localDateTime\":\"2017-01-02T03:04:05Z\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getLocalDateTime(), LocalDateTime.of(2017, 1, 2, 3, 4, 5));
  }

  @Test
  public void deserializeLocalDateTimeOffset() {
    String json = "{\"localDateTime\":\"2017-01-02T03:04:05+01:00\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getLocalDateTime(), LocalDateTime.of(2017, 1, 2, 2, 4, 5));
  }

  @Test
  public void deserializeZonedDateTimeProper() {
    String json = "{\"zonedDateTime\":\"2017-01-02T03:04:05+03:00\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getZonedDateTime(),
        ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.ofHours(3)));
  }

  @Test
  public void deserializeLocalDateProper() {
    String json = "{\"localDate\":\"2017-01-02\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getLocalDate(), LocalDate.of(2017, 1, 2));
  }

  @Test
  public void deserializeNulls() {
    String json = "{\"localDate\":null,\"localDateTime\":null,\"zonedDateTime\":null}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertNull(dateRelated.getLocalDate());
    assertNull(dateRelated.getLocalDateTime());
    assertNull(dateRelated.getZonedDateTime());
  }

}
