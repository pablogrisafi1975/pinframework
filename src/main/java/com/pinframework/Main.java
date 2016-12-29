package com.pinframework;

import java.util.Map;

import com.pinframework.response.PinResponseOkText;

public class Main {
	public static void main(String[] args) {

		PinServer pinServer = new PinServerBuilder().appContext("app").port(3030).build();
		pinServer.on(new PinRequestMatcher() {
			@Override
			public boolean matches(String verb, String route, String contentType) {
				return "GET".equals(verb) && "/app/hello".equals(route);
			}

			@Override
			public Map<String, String> extractPathParams(String route) {
				return null;
			}
		}, pinExchange -> PinResponseOkText.of("hello-ok"));

		pinServer.onGet("hello2", pinExchange -> PinResponseOkText.of("hello2-ok"));

		pinServer.onGet("hello3/sub1", pinExchange -> PinResponseOkText.of("hello3-ok-sub1"));
		pinServer.onGet("hello4/:param1",
				pinExchange -> PinResponseOkText.of("hello4-ok-" + pinExchange.getPathParams().get("param1")));
		pinServer.onGet("hello5/:param1/nada/:param2/nada",
				pinExchange -> PinResponseOkText.of("hello5-ok-" + pinExchange.getPathParams().get("param1") + "+" + pinExchange.getPathParams().get("param2")));

		pinServer.start();
	}
}
