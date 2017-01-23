package com.pinframework.response;

import com.pinframework.PinContentType;
import com.pinframework.PinExchange;
import com.pinframework.PinGson;
import com.pinframework.PinResponse;
import com.pinframework.PinUtils;
import com.pinframework.exception.PinIORuntimeException;
import com.pinframework.render.PinRenderNull;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class PinResponseSse extends PinResponse {
  private final PrintWriter printWriter;
  private final PinExchange pinExchange;

  /**
   * Creates a Server Sent Event Response.
   * 
   * @param pinExchange The exchange handled by the response
   */
  public PinResponseSse(PinExchange pinExchange) {
    super(HttpURLConnection.HTTP_OK, pinExchange, PinRenderNull.INSTANCE);
    this.pinExchange = pinExchange;
    HttpExchange httpExchange = pinExchange.raw();
    httpExchange.getResponseHeaders().add(PinContentType.CONTENT_TYPE,
        PinContentType.TEXT_EVENT_STREAM);
    httpExchange.getResponseHeaders().add("CharacterEncoding", "UTF-8");
    httpExchange.getResponseHeaders().add("cache-control", "no-cache");
    httpExchange.getResponseHeaders().add("connection", "keep-alive");
    try {
      httpExchange.sendResponseHeaders(200, 0);
      httpExchange.getResponseBody().flush();
      PinUtils.fullyRead(httpExchange.getRequestBody());
    } catch (IOException ex) {
      throw new PinIORuntimeException(ex);
    }
    this.printWriter = new PrintWriter(
        new OutputStreamWriter(httpExchange.getResponseBody(), StandardCharsets.UTF_8), false);
  }

  /**
   * @param data An object to be rendered a JSON.
   * @return true is sent without errors, false if couldn't send data
   */
  public boolean sendObject(Object data) {
    String jsonData = PinGson.getInstance().toJson(data);
    return send(jsonData);
  }

  public boolean sendObject(String event, Object data) {
    String jsonData = PinGson.getInstance().toJson(data);
    return send(event, jsonData);
  }

  public boolean send(String data) {
    return send(null, data);
  }

  /**
   * @param data A string to be sent.
   * @return true is sent without errors, false if couldn't send data
   */
  public boolean send(String event, String data) {
    if (event != null) {
      String next = "event: " + event + "\n";
      printWriter.append(next);
      if (printWriter.checkError()) {
        return false;
      }
    }
    // TODO: keep alive, id on retry
    String[] lines = data.split("\n", -1);
    for (String line : lines) {
      String next = "data: " + line + "\n";
      printWriter.append(next);
      if (printWriter.checkError()) {
        return false;
      }
    }
    printWriter.append("\n");
    return !printWriter.checkError();
  }

  @Override
  public boolean keepResponseOpen() {
    return true;
  }

  /**
   * Send a comment. Comments can not be seen as events.
   * 
   * @param comment The comment to send
   * @return true is sent, false if any problem
   */
  public boolean comment(String comment) {
    String next = ": " + comment + "\n\n";
    printWriter.append(next);
    return !printWriter.checkError();

  }

  /**
   * Closes the exchange. No message can be sent after close.
   */
  public void close() {
    try {
      pinExchange.raw().getResponseBody().close();
    } catch (IOException ex) {
      throw new PinIORuntimeException(ex);
    }
  }

}
