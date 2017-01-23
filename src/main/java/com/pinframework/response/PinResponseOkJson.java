package com.pinframework.response;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJson;

import java.net.HttpURLConnection;

public class PinResponseOkJson extends PinResponse {

  public PinResponseOkJson(Object obj) {
    super(HttpURLConnection.HTTP_OK, obj, PinRenderJson.INSTANCE);
  }
}
