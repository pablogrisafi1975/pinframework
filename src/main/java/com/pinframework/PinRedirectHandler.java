package com.pinframework;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class PinRedirectHandler implements HttpHandler {

	private final Map<PinRequestMatcher, PinHandler> routeMap = Collections.synchronizedMap(new LinkedHashMap<>());

	public void on(PinRequestMatcher requestPredicate, PinHandler handler) {
		routeMap.put(requestPredicate, handler);
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String route = httpExchange.getRequestURI().getPath();
		String verb = httpExchange.getRequestMethod();
		String contentType = httpExchange.getRequestHeaders().getFirst("ContentType");

		for (Map.Entry<PinRequestMatcher, PinHandler> entry : routeMap.entrySet()) {
			PinRequestMatcher requestMatcher = entry.getKey();
			if (requestMatcher.matches(verb, route, contentType)) {
				PinHandler pinHandler = entry.getValue();
				process(route, httpExchange, requestMatcher, pinHandler);
				return;
			}
		}

		PinHandler notFoundHandler = new NotFoundHandler();
		process(route, httpExchange, null, notFoundHandler);
	}

	private void process(String route, HttpExchange httpExchange, PinRequestMatcher requestMatcher,
			PinHandler pinHandler) {
		Map<String, String> pathParams = requestMatcher == null ? Collections.emptyMap()
				: requestMatcher.extractPathParams(route);

		PinExchange pinExchange = new PinExchange(httpExchange, pathParams);
		boolean keepResponseOpen = false;
		try {
			PinResponse pinResponse = pinHandler.handle(pinExchange);
			PinRender pinTransformer = pinResponse.getTransformer();
			pinTransformer.changeHeaders(httpExchange.getResponseHeaders());
			keepResponseOpen = pinResponse.keepResponseOpen();
			if (!keepResponseOpen) {
				httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
			}
			pinTransformer.render(pinResponse.getObj(), httpExchange.getResponseBody());
		} catch (Exception e) {
			// TODO usar los exception handlers
			e.printStackTrace();
		} finally {
			if (!keepResponseOpen) {
				httpExchange.close();
			}
		}
	}

}
