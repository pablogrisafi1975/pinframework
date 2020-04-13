package com.pinframework;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.pinframework.exceptions.PinBadRequestException;

public class PinGsonBuilderFactory {
    //TODO: manejar mas tipos de fecha, ida y vuelta
    /**
     * Makes a GsonBuilder that
     * serializes LocalDateTime as yyyy-MM-ddTHH:mm:ssZ
     * serializes Exception as type and message.
     *
     * Returns a builder so you can call build() directly or keep on configuring things
     */

    public static GsonBuilder make() {
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
                .registerTypeAdapter(PinBadRequestException.class,
                        (JsonSerializer<PinBadRequestException>) (src, typeOfSrc, context) -> {
                            var json = new JsonObject();
                            json.addProperty("type", src.getClass().getName());
                            json.addProperty("message", src.getMessage());
                            json.addProperty("messageKey", src.getMessageKey());
                            json.addProperty("fieldName", src.getFieldName());
                            json.addProperty("currentValue", src.getCurrentValue());
                            json.addProperty("destinationClassName", src.getDestinationClassName());
                            return json;
                        });
    }
}
