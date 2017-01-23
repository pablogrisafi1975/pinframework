package com.pinframework.response;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderTextUtf8;

import java.net.HttpURLConnection;

public class PinResponseNotFoundText extends PinResponse {

  public PinResponseNotFoundText(String text) {
    super(HttpURLConnection.HTTP_NOT_FOUND, text, PinRenderTextUtf8.INSTANCE);
  }
}
