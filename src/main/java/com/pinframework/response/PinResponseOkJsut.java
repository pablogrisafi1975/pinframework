package com.pinframework.response;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJsut;

import java.net.HttpURLConnection;

public class PinResponseOkJsut extends PinResponse {

  public PinResponseOkJsut(Object obj, String template) {
    super(HttpURLConnection.HTTP_OK, new PinRenderJsut.Input(obj, template),
        PinRenderJsut.INSTANCE);
  }
}
