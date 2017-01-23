package com.pinframework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
  public static void main(String[] args) {

    PinServer pinServer =
        new PinServerBuilder().appContext("app").port(3030).externalFolder("C:/git/MiWeb").build();
    pinServer.on(new PinRequestMatcher() {
      @Override
      public boolean matches(String verb, String route, String contentType) {
        return "GET".equals(verb) && "/app/hello".equals(route);
      }

      @Override
      public Map<String, String> extractPathParams(String route) {
        return Collections.emptyMap();
      }
    }, pinExchange -> PinResponses.okText("hello-ok"));

    pinServer.onGet("hello2", pinExchange -> PinResponses.okText("hello2-ok"));

    pinServer.onGet("hello3/sub1", pinExchange -> PinResponses.okText("hello3-ok-sub1"));
    pinServer.onGet("hello4/:param1", pinExchange -> PinResponses
        .okText("hello4-ok-" + pinExchange.getPathParams().get("param1")));
    pinServer.onGet("hello5/:param1/nada/:param2/nada",
        pinExchange -> PinResponses.okText("hello5-ok-" + pinExchange.getPathParams().get("param1")
            + "-" + pinExchange.getPathParams().get("param2")));

    Map<String, String> map = new HashMap<>();
    map.put("key0", "value0");
    map.put("key1", "value1");

    pinServer.onGet("hello6/json", pinExchange -> PinResponses.okJson(map));

    pinServer.onGet("hello7/down",
        pinExchange -> PinResponses.okDownload("el ñu es lindo", "fileName.txt"));

    pinServer.start();
  }
}
