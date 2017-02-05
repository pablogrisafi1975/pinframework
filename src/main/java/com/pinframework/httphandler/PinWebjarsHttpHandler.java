package com.pinframework.httphandler;

import com.pinframework.PinMimeType;
import com.pinframework.PinUtils;
import com.pinframework.constant.PinContentType;
import com.pinframework.constant.PinHeader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinWebjarsHttpHandler implements HttpHandler {

  private static final String WEBJARS_PATH = "META-INF/resources/webjars";
  private static final Logger LOG = LoggerFactory.getLogger(PinWebjarsHttpHandler.class);
  private final boolean preferMinified;


  public PinWebjarsHttpHandler(boolean preferMinified) {
    this.preferMinified = preferMinified;
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {

    if (!"GET".equals(httpExchange.getRequestMethod())) {
      String message = "Error trying to access '" + httpExchange.getRequestURI().getPath()
          + "', wrong method '" + httpExchange.getRequestMethod() + "'";
      LOG.error(message);
      httpExchange.getResponseHeaders().add(PinHeader.CONTENT_TYPE, PinContentType.TEXT_PLAIN_UTF8);
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
      httpExchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
      httpExchange.close();
      return;
    }
    String filename = httpExchange.getRequestURI().getPath()
        .replaceFirst("\\Q" + httpExchange.getHttpContext().getPath() + "\\E", "");

    try (InputStream is = findInputStream(filename)) {

      if (is == null) {
        httpExchange.getResponseHeaders().add(PinHeader.CONTENT_TYPE,
            PinContentType.TEXT_PLAIN_UTF8);
        httpExchange.getResponseHeaders().add(PinHeader.CONNECTION, "close");
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        httpExchange.getResponseBody().write(
            ("File '" + filename + "' not found in webjars").getBytes(StandardCharsets.UTF_8));
        LOG.error("File not found for request '{}'", httpExchange.getRequestURI().getPath());
      } else {
        String mimeType = PinMimeType.fromFileName(filename);
        httpExchange.getResponseHeaders().add(PinHeader.CONTENT_TYPE, mimeType);
        httpExchange.getResponseHeaders().add(PinHeader.CONNECTION, "close");
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        PinUtils.copy(is, httpExchange.getResponseBody());
      }
    } catch (Exception ex) {
      httpExchange.getResponseHeaders().add(PinHeader.CONNECTION, "close");
      httpExchange.getResponseHeaders().add(PinHeader.CONTENT_TYPE, PinContentType.TEXT_PLAIN_UTF8);
      LOG.error("Error on request uri '{}'", httpExchange.getRequestURI().getPath(), ex);
      httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
    } finally {
      httpExchange.close();
    }
  }

  private InputStream findInputStream(String fileName) throws IOException {
    if (preferMinified) {
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
