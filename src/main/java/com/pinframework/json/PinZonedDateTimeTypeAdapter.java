package com.pinframework.json;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PinZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {
    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        out.value(value.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        return ZonedDateTime.parse(in.nextString(), DateTimeFormatter.ISO_DATE_TIME);
    }
}
