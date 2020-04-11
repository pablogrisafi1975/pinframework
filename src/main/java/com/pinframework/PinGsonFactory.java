package com.pinframework;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class PinGsonFactory {
    //TODO: manejar mas tipos de fecha, ida y vuelta
    /**
     * Builds a GSON that
     * serializes LocalDateTime as yyyy-MM-ddTHH:mm:ssZ
     * serializes Exception as type and message
     */

    public static Gson build() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(
                                src.format(DateTimeFormatter.ISO_DATE_TIME)))
                .registerTypeHierarchyAdapter(Exception.class,
                        (JsonSerializer<Exception>) (src, typeOfSrc, context) -> {
                            var json = new JsonObject();
                            json.addProperty("type", src.getClass().getName());
                            json.addProperty("message", src.getMessage());
                            return json;
                        })
                .create();
    }
}
