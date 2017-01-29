package com.pinframework.httphandler;

import com.pinframework.PinContentType;
import com.pinframework.PinMimeType;
import com.pinframework.PinServer;
import com.pinframework.PinUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;

public class PinWebjarsHttpHandler implements HttpHandler {

  private static final String WEBJARS_PATH = "META-INF/resources/webjars";
  private static final Logger LOG = LoggerFactory.getLogger(PinWebjarsHttpHandler.class);
  private final boolean autoMinimize;


  public PinWebjarsHttpHandler(boolean autoMinimize) {
    this.autoMinimize = autoMinimize;
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {

    if (!"GET".equals(httpExchange.getRequestMethod())) {
      LOG.error("Error trying to access '{}', wrong method '{}'",
          httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
      httpExchange.close();
      return;
    }
    String filename = httpExchange.getRequestURI().getPath()
        .replaceFirst("\\Q" + httpExchange.getHttpContext().getPath() + "\\E", "");

    // filename /angularjs/1.4.0/angular.min.js

    try (InputStream is = findInputStream(filename)) {

      if (is == null) {
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        httpExchange.getResponseBody().write(
            ("File '" + filename + "' not found in webjars").getBytes(StandardCharsets.UTF_8));
        LOG.error("File not found for request '{}'", httpExchange.getRequestURI().getPath());
      } else {
        String mimeType = PinMimeType.fromFileName(filename);
        httpExchange.getResponseHeaders().add(PinContentType.CONTENT_TYPE, mimeType);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        PinUtils.copy(is, httpExchange.getResponseBody());
      }
    } catch (Exception e) {
      LOG.error("Error on request uri '{}'", httpExchange.getRequestURI().getPath(), e);
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
    }
    httpExchange.close();
  }

  private InputStream findInputStream(String fileName) throws IOException {
    if (autoMinimize) {
      int lastIndexOfDot = fileName.lastIndexOf('.');
      String fileNameNoExt = fileName.substring(0, lastIndexOfDot);
      if (!fileNameNoExt.endsWith(".min")) { // already minimized
        String fileExt = fileName.substring(lastIndexOfDot);
        String minimizedFileName = fileNameNoExt + ".min" + fileExt;
        InputStream minimizedStream =
            PinUtils.getResourceAsStream(WEBJARS_PATH + minimizedFileName);
        if (minimizedStream != null) {
          return minimizedStream;
        }
      } else {
        LOG.warn("Asking for an already minimized file '{}' while webjarsAutoMinimize is true",
            fileName);
      }
    }
    return PinUtils.getResourceAsStream(WEBJARS_PATH + fileName);
  }

}
