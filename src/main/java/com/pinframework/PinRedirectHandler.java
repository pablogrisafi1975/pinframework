package com.pinframework;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.pinframework.requestmatcher.PinNotFoundRequestMatcher;
import com.pinframework.upload.FileParam;
import com.pinframework.upload.MultipartParams;
import com.pinframework.upload.PinMutipartParamsParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class PinRedirectHandler implements HttpHandler {

	private final Map<PinRequestMatcher, PinHandler> routeMap = Collections.synchronizedMap(new LinkedHashMap<>());

	private final PinHandler notFoundHandler = new PinNotFoundHandler();

	private final PinRequestMatcher notFoundRequestMatcher = new PinNotFoundRequestMatcher();

	private final PinParamsParser paramsParser;

	private final PinMutipartParamsParser multipartParamsParser;

	private final boolean uploadSupportEnabled;

	public PinRedirectHandler(Gson gsonParser, boolean uploadSupportEnabled) {
		this.uploadSupportEnabled = uploadSupportEnabled;
		this.paramsParser = new PinParamsParser(gsonParser);
		this.multipartParamsParser = uploadSupportEnabled ? PinMutipartParamsParser.createImpl() : null;
	}

	public void on(PinRequestMatcher requestPredicate, PinHandler handler) {
		routeMap.put(requestPredicate, handler);
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String route = httpExchange.getRequestURI().getPath();
		String verb = httpExchange.getRequestMethod();
		String contentType = httpExchange.getRequestHeaders().getFirst(PinMimeType.CONTENT_TYPE);

		for (Map.Entry<PinRequestMatcher, PinHandler> entry : routeMap.entrySet()) {
			PinRequestMatcher requestMatcher = entry.getKey();
			if (requestMatcher.matches(verb, route, contentType)) {
				PinHandler pinHandler = entry.getValue();
				process(route, httpExchange, requestMatcher, pinHandler);
				return;
			}
		}

		process(route, httpExchange, notFoundRequestMatcher, notFoundHandler);
	}

	private void process(String route, HttpExchange httpExchange, PinRequestMatcher requestMatcher,
			PinHandler pinHandler) {
		Map<String, String> pathParams = requestMatcher.extractPathParams(route);

		Map<String, List<String>> queryParams = paramsParser.queryParams(httpExchange.getRequestURI().getQuery());

		String fullContentType = httpExchange.getRequestHeaders().getFirst(PinMimeType.CONTENT_TYPE);

		Map<String, Object> postParams = null;
		Map<String, FileParam> fileParams = Collections.emptyMap();
		if (paramsParser.isMultipart(fullContentType)) {
			if (uploadSupportEnabled) {
				MultipartParams multipartParams = multipartParamsParser.parse(httpExchange);
				postParams = multipartParams.getPostParams();
				fileParams = multipartParams.getFileParams();
			} else {
				// TODO: log.error()
			}
		} else {
			postParams = paramsParser.postParams(fullContentType, httpExchange.getRequestBody());
		}

		PinExchange pinExchange = new PinExchange(httpExchange, pathParams, queryParams, postParams, fileParams);
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
