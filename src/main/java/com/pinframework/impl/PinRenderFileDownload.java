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
import com.pinframework.PinRenderType;
import com.pinframework.PinUtils;

/**
 * You need to manually set the file name, using<br>
 *     <code>ex.writeDownloadFileName(fileName);<code/>
 */
public class PinRenderFileDownload implements PinRender {

    @Override
    public String getType() {
        return PinRenderType.DOWNLOAD;
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        PinUtils.copy((InputStream) obj, outputStream);

    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, PinContentType.APPLICATION_FORCE_DOWNLOAD);
    }

}
