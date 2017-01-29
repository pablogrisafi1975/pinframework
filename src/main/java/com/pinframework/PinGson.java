package com.pinframework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc,
            JsonSerializationContext context) {
          return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
        }
      })
      .registerTypeAdapter(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
        @Override
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc,
            JsonSerializationContext context) {
          return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
        }
      })
      .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc,
            JsonSerializationContext context) {
          return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE));
        }
      })
      
      .create();


  public static Gson getInstance() {
    return GSON;
  }
}
