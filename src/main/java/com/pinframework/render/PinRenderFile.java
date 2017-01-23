package com.pinframework.render;

import com.pinframework.PinContentType;
import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class PinRenderFile implements PinRender {

  private final String fileName;
  private final boolean download;

  public PinRenderFile(String fileName, boolean download) {
    this.fileName = fileName;
    this.download = download;
  }

  @Override
  public void render(Object obj, OutputStream outputStream) throws IOException {
    PinUtils.copy((InputStream) obj, outputStream);
  }

  @Override
  public void changeHeaders(Map<String, List<String>> responseHeaders) {
    if (download) {
      PinUtils.put(responseHeaders, "Content-Disposition",
          "attachment; filename=\"" + PinUtils.urlEncode(fileName) + "\";");
      PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE,
          PinContentType.APPLICATION_FORCE_DOWNLOAD);
    } else {
      String mimeType = PinMimeType.fromFileName(fileName);
      PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, mimeType);
    }
  }

}
