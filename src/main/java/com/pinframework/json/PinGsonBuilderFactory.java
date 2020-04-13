package com.pinframework.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.pinframework.exceptions.PinBadRequestException;

public final class PinGsonBuilderFactory {

    private PinGsonBuilderFactory() {
    }

    /**
     * Makes a GsonBuilder that
     * serializes LocalDateTime as yyyy-MM-ddTHH:mm:ssZ
     * serializes Exception as type and message.
     * <p>
     * Returns a builder so you can call build() directly or keep on configuring things
     */

    public static GsonBuilder make() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(LocalDateTime.class, new PinLocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class, new PinLocalDateTypeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new PinZonedDateTimeTypeAdapter().nullSafe())
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
