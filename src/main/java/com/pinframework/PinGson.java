package com.pinframework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class holds the default Gson instance that will be used for convert objects to-from json. It
 * has support several date/time related classes, using ISO_DATE_TIME = yyyy-MM-ddTHH:mm:ssZ and You
 * can set a different configuration in the server builder
 * 
 * @author Pablo
 *
 */
public class PinGson {
  private static final Gson GSON =
      new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.value(value.format(DateTimeFormatter.ISO_DATE_TIME));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          String strValue = in.nextString();
          try {
            return LocalDateTime.parse(strValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          } catch (java.time.format.DateTimeParseException ex) {
            return ZonedDateTime.parse(strValue, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
          }
        }
      }).registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {

        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.value(value.format(DateTimeFormatter.ISO_DATE_TIME));
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ISO_DATE_TIME);
        }
      }).registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
          if (value == null) {
            out.nullValue();
            return;
          }
          out.value(value.format(DateTimeFormatter.ISO_DATE));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
          if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
          }
          return LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_DATE);
        }
      })

          .create();


  public static Gson getInstance() {
    return GSON;
  }
}
