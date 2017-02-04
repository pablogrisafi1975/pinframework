package com.pinframework.render;

import com.pinframework.PinRender;
import com.pinframework.PinUtils;
import com.pinframework.constant.PinContentType;
import com.pinframework.constant.PinHeader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PinRenderTextUtf8 implements PinRender {


  @Override
  public void render(Object obj, OutputStream outputStream) throws IOException {
    if (obj != null) {
      PrintWriter pw =
          new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
      pw.write(obj.toString());
      pw.flush();
      pw.close();
    }
  }

  @Override
  public void changeHeaders(Map<String, List<String>> responseHeaders) {
    PinUtils.put(responseHeaders, PinHeader.CONTENT_TYPE, PinContentType.TEXT_PLAIN_UTF8);
  }

}
