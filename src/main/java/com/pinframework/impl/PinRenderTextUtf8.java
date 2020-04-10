package com.pinframework.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

public class PinRenderTextUtf8 implements PinRender {

    public static final PinRenderTextUtf8 INSTANCE = new PinRenderTextUtf8();

    @Override
    public void render(Object obj, OutputStream outputStream) {
        if (obj != null) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
            pw.write(obj.toString());
            pw.flush();
            pw.close();
        }
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, PinMimeType.CONTENT_TYPE, "text/plain; charset=utf-8");
    }

}
