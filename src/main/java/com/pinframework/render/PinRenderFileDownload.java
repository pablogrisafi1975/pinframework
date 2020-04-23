package com.pinframework.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.pinframework.PinContentType;
import com.pinframework.PinRender;
import com.pinframework.PinRenderType;
import com.pinframework.PinUtils;

/**
 * You need to manually set the file name, using<br>
 * <code>ex.writeDownloadFileName(fileName);<code/><br>
 * You need to manually write the content using<br>
 *     <code>ex.writeResponseContentLine(...);<code/> or <code>ex.writeResponseContent(...);<code/>
 *
 */
public class PinRenderFileDownload implements PinRender {

    @Override
    public String getType() {
        return PinRenderType.DOWNLOAD;
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        outputStream.close();
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, PinContentType.APPLICATION_FORCE_DOWNLOAD);
    }

}
