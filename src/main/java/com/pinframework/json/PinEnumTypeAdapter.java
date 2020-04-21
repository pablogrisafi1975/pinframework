package com.pinframework.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A copy of com.google.gson.internal.bind.TypeAdapters$EnumTypeAdapter
 * that throws an exception on invalid values
 *
 * @param <T>
 */
public final class PinEnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
    private final Map<String, T> nameToConstant = new HashMap<>();
    private final Map<T, String> constantToName = new HashMap<>();
    private final Class<T> enumClass;

    public PinEnumTypeAdapter(Class<T> classOfT) {
        this.enumClass = classOfT;
        try {
            for (T constant : classOfT.getEnumConstants()) {
                String name = constant.name();
                SerializedName annotation = classOfT.getField(name).getAnnotation(SerializedName.class);
                if (annotation != null) {
                    name = annotation.value();
                    for (String alternate : annotation.alternate()) {
                        nameToConstant.put(alternate, constant);
                    }
                }
                nameToConstant.put(name, constant);
                constantToName.put(constant, name);
            }
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String stringValue = in.nextString();
        T enumValue = nameToConstant.get(stringValue);
        if (enumValue == null) {
            throw new JsonParseException(new IllegalArgumentException("Can not deserialize " + stringValue + " to " + enumClass.getName()));
        }
        return enumValue;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.value(value == null ? null : constantToName.get(value));
    }
}