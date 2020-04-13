package com.pinframework.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.pinframework.PinContentType;
import com.pinframework.PinRender;
import com.pinframework.PinRenderType;
import com.pinframework.PinUtils;

public class PinRenderJson implements PinRender {

    private final Gson gson;

    public PinRenderJson(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    @Override
    public String getType() {
        return PinRenderType.JSON;
    }

    public String render(Object model) {
        return gson.toJson(model);
    }

    public void render(Object model, Appendable writer) {
        gson.toJson(model, writer);
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        if (obj == null) {
            outputStream.close();
        } else {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                gson.toJson(obj, writer);
                writer.flush();
            }
        }
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, PinContentType.APPLICATION_JSON_UTF8);
    }

}
