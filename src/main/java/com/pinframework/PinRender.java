package com.pinframework;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface PinRender {
  // What about a csvrender, or a xls render?
  void render(Object obj, OutputStream outputStream) throws Exception;

  default void changeHeaders(Map<String, List<String>> responseHeaders) {

  }

}
