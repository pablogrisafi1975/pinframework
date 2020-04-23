package com.pinframework.render;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.pinframework.PinContentType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

public abstract class PinAbstractRender implements PinRender {

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        if (obj == null) {
            outputStream.close();
        } else {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 4096)) {
                writeNonNullObject(writer, obj);
                writer.flush();
            }
        }
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        String newContentType = getNewContentType();
        if (newContentType != null) {
            PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, newContentType);
        }
    }

    protected abstract String getNewContentType();

    protected abstract void writeNonNullObject(BufferedWriter writer, Object obj) throws IOException;

}
