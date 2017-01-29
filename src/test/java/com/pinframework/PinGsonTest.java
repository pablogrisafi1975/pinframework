package com.pinframework;

import static org.testng.Assert.assertEquals;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.testng.annotations.Test;

@Test
public class PinGsonTest {
  private Gson pinGson = PinGson.getInstance();

  public void serializeLocalDateTime() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setLocalDateTime(LocalDateTime.of(2017, 1, 2, 3, 4, 5));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"localDateTime\":\"2017-01-02T03:04:05\"}");
  }

  public void serializeZonedDateTime() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setZonedDateTime(ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.ofHours(3)));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"zonedDateTime\":\"2017-01-02T03:04:05+03:00\"}");
  }
  
  public void serializeZonedDateTimeUTC() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setZonedDateTime(ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"zonedDateTime\":\"2017-01-02T03:04:05Z\"}");
  }

  public void serializeLocalDate() {
    DateRelated dateRelated = new DateRelated();
    dateRelated.setLocalDate(LocalDate.of(2017, 1, 2));
    String json = pinGson.toJson(dateRelated);
    assertEquals(json, "{\"localDate\":\"2017-01-02\"}");
  }
  
  public void deserializeLocalDateTimeProper() {
    String json = "{\"localDateTime\":\"2017-01-02T03:04:05\"}";
    DateRelated dateRelated = pinGson.fromJson(json, DateRelated.class);
    assertEquals(dateRelated.getLocalDateTime(), LocalDateTime.of(2017, 1, 2, 3, 4, 5));
  }

}
