package com.pinframework.render;

import com.pinframework.PinContentType;
import com.pinframework.PinGson;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class PinRenderJson implements PinRender {

  public static final PinRenderJson INSTANCE = new PinRenderJson();

  public String render(Object model) {
    return PinGson.getInstance().toJson(model);
  }

  public void render(Object model, Appendable writter) {
    PinGson.getInstance().toJson(model, writter);
  }

  @Override
  public void render(Object obj, OutputStream outputStream) throws IOException {
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    PinGson.getInstance().toJson(obj, writer);
    writer.flush();
    writer.close();
  }

  @Override
  public void changeHeaders(Map<String, List<String>> responseHeaders) {
    PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE,
        PinContentType.APPLICATION_JSON_UTF8);
  }

}
