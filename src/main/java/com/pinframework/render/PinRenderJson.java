package com.pinframework.render;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Objects;

import com.google.gson.Gson;
import com.pinframework.PinContentType;
import com.pinframework.PinRenderType;

public class PinRenderJson extends PinAbstractRender {

    private final Gson gson;

    public PinRenderJson(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    @Override
    public String getType() {
        return PinRenderType.JSON;
    }

    @Override
    protected String getNewContentType() {
        return PinContentType.APPLICATION_JSON_UTF8;
    }

    @Override
    protected void writeNonNullObject(BufferedWriter writer, Object obj) throws IOException {
        gson.toJson(obj, writer);
    }

}
