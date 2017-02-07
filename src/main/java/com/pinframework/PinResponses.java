package com.pinframework;

import com.pinframework.render.PinRenderFile;
import com.pinframework.render.PinRenderJson;
import com.pinframework.render.PinRenderJsut;
import com.pinframework.render.PinRenderTextUtf8;
import com.pinframework.response.PinBaseResponse;
import com.pinframework.response.PinResponseSse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.input.CharSequenceInputStream;

public final class PinResponses {

  private static final PinRenderTextUtf8 PIN_RENDER_TEXT_UTF_8 = new PinRenderTextUtf8();

  private static final PinRenderJson PIN_RENDER_JSON = new PinRenderJson();

  private PinResponses() {
    // do nothing, shut up sonar
  }

  public static PinResponse okText(String text) {
    return new PinBaseResponse(HttpURLConnection.HTTP_OK, text, PIN_RENDER_TEXT_UTF_8);
  }

  public static PinResponse okJson(Object obj) {
    return new PinBaseResponse(HttpURLConnection.HTTP_OK, obj, PIN_RENDER_JSON);
  }

  public static PinResponse okJsut(Object obj, String template) {
    return new PinBaseResponse(HttpURLConnection.HTTP_OK, new PinRenderJsut.Input(obj, template),
        PinRenderJsut.INSTANCE);
  }

  public static PinResponse okDownload(InputStream inputStream, String fileName) {
    return new PinBaseResponse(HttpURLConnection.HTTP_OK, inputStream,
        new PinRenderFile(fileName, true));
  }

  public static PinResponse okDownload(String text, String fileName) {
    InputStream inputStream = new CharSequenceInputStream(text, StandardCharsets.UTF_8);
    return okDownload(inputStream, fileName);
  }

  public static PinResponse notFoundText(String text) {
    return new PinBaseResponse(HttpURLConnection.HTTP_NOT_FOUND, text, PIN_RENDER_TEXT_UTF_8);
  }

  public static PinResponse notFoundJson(Object obj) {
    return new PinBaseResponse(HttpURLConnection.HTTP_NOT_FOUND, obj, PIN_RENDER_JSON);
  }

  public static PinResponse okFile(InputStream inputStream, String fileName) {
    return new PinBaseResponse(HttpURLConnection.HTTP_OK, inputStream,
        new PinRenderFile(fileName, false));
  }

  public static PinResponseSse okSse(PinExchange pex) {
    return new PinResponseSse(pex);
  }
}
