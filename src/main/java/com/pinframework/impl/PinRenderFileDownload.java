package com.pinframework.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.pinframework.PinContentType;
import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

public class PinRenderFileDownload implements PinRender {

    private final String fileName;

    public PinRenderFileDownload(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        PinUtils.copy((InputStream) obj, outputStream);

    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, "Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\";");
        PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, "application/force-download");

    }

}
