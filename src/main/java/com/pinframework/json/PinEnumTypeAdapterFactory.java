package com.pinframework.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class PinEnumTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
            return null;
        }
        if (!rawType.isEnum()) {
            rawType = rawType.getSuperclass(); // handle anonymous subclasses
        }

        return (TypeAdapter<T>) new PinEnumTypeAdapter(rawType);
    }

}
