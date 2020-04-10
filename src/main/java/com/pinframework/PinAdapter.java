package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pinframework.exceptions.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PinAdapter implements HttpHandler {
	private static final Logger LOG = LoggerFactory.getLogger(PinAdapter.class);


	private final Map<String, PinHandler> handlerByMethod = new HashMap<>();
	private final Map<String, List<String>> parameterNamesByMethod = new HashMap<>();

	public PinAdapter(String method, List<String> pathParameterNames, PinHandler pinHandler) {
		handlerByMethod.put(method, pinHandler);
		parameterNamesByMethod.put(method, pathParameterNames);
	}

	public void put(String method, List<String> pathParameterNames, PinHandler pinHandler) {
		if (handlerByMethod.containsKey(method)) {
			throw new PinInitializationException("PinHandler already present for route and method = '" + method + "'");
		}
		handlerByMethod.put(method, pinHandler);
		parameterNamesByMethod.put(method, pathParameterNames);
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String method = httpExchange.getRequestMethod();
		PinHandler pinHandler = handlerByMethod.get(method);
		if (pinHandler == null) {
			LOG.error("Error trying to access '{}', wrong method '{}'", httpExchange.getRequestURI().getPath(),
					method);
			httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			httpExchange.close();
			return;
		}
		PinExchange pinExchange = new PinExchange(httpExchange, parameterNamesByMethod.get(method));
		boolean keepResponseOpen = false;
		try {
			PinResponse pinResponse = pinHandler.handle(pinExchange);
			PinRender pinTransformer = pinResponse.getTransformer();
			pinTransformer.changeHeaders(httpExchange.getResponseHeaders());
			keepResponseOpen = pinResponse.keepResponseOpen();
			if(!keepResponseOpen){
				httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
			}
			pinTransformer.render(pinResponse.getObj(), httpExchange.getResponseBody());
		} catch (Exception ex) {
			httpExchange.sendResponseHeaders(503, 0);
			// TODO: log y crear un exception transformer que pueda mostra la
			// excepcion como json, texto o nada
			httpExchange.getResponseBody()
					.write(("UnexpectedException " + ex.toString()).getBytes(StandardCharsets.UTF_8));
		} finally {
			if (!keepResponseOpen) {
				httpExchange.close();
			}
		}
	}

}
