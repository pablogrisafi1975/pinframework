package com.pinframework.response;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderTextUtf8;

import java.net.HttpURLConnection;

public class PinResponseOkText extends PinResponse {

  public PinResponseOkText(String text) {
    super(HttpURLConnection.HTTP_OK, text, PinRenderTextUtf8.INSTANCE);
  }
}
