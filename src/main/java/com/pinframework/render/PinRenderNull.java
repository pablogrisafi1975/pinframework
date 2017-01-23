package com.pinframework.render;

import com.pinframework.PinRender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class PinRenderNull implements PinRender {

  public static final PinRenderNull INSTANCE = new PinRenderNull();

  @Override
  public void render(Object obj, OutputStream outputStream) throws IOException {
    // Server sent events do not render data on close

  }

  @Override
  public void changeHeaders(Map<String, List<String>> responseHeaders) {
    // Server sent events do not render data on close
  }

}
