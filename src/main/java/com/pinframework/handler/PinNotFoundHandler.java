package com.pinframework.handler;

import com.pinframework.PinContentType;
import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinResponse;
import com.pinframework.PinResponses;
import com.pinframework.PinUtils;

import java.util.HashMap;
import java.util.Map;

public class PinNotFoundHandler implements PinHandler {

  @Override
  public PinResponse handle(PinExchange pinExchange) throws Exception {
    String contentType =
        PinUtils.getFirst(pinExchange.getRequestHeaders(), PinContentType.CONTENT_TYPE);
    String requestUri = pinExchange.raw().getRequestURI().normalize().toString();
    if (contentType != null && contentType.startsWith(PinContentType.APPLICATION_JSON)) {
      Map<String, String> map = new HashMap<>();
      map.put("requestUri", requestUri);
      return PinResponses.notFoundJson(map);
    }
    String text = "Can not find " + requestUri;
    return PinResponses.notFoundText(text);
  }

}
