package com.pinframework.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.pinframework.PinContentType;
import com.pinframework.PinRender;
import com.pinframework.PinRenderType;
import com.pinframework.PinUtils;

public class PinRenderText implements PinRender {

    @Override
    public String getType() {
        return PinRenderType.TEXT;
    }

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
        PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, PinContentType.TEXT_PLAIN_UTF8);
    }

}
