package com.pinframework.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

public class PinRenderJson implements PinRender {

    public static final PinRenderJson INSTANCE = new PinRenderJson();

    public String render(Object model) {
        return PinUtils.GSON.toJson(model);
    }

    public void render(Object model, Appendable writer) {
        PinUtils.GSON.toJson(model, writer);
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        PinUtils.GSON.toJson(obj, writer);
        writer.flush();
        writer.close();
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, PinMimeType.CONTENT_TYPE, PinMimeType.APPLICATION_JSON_UTF8);
    }

}
