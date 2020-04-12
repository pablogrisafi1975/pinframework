package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;
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
    private final PinRender pinRender;

    public PinAdapter(String method, List<String> pathParameterNames, PinHandler pinHandler, PinRender pinRender) {
        handlerByMethod.put(method, pinHandler);
        parameterNamesByMethod.put(method, pathParameterNames);
        this.pinRender = pinRender;
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
            pinRender.changeHeaders(httpExchange.getResponseHeaders());
            keepResponseOpen = pinResponse.keepResponseOpen();
            if (!keepResponseOpen) {
                httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
            }
            pinRender.render(pinResponse.getObj(), httpExchange.getResponseBody());
        } catch (Exception ex) {
            LOG.error("Unexpected exception, will return INTERNAL_SERVER_ERROR=500", ex);
            try {
                pinRender.changeHeaders(httpExchange.getResponseHeaders());
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
                pinRender.render(ex, httpExchange.getResponseBody());
            } catch (Exception ex2) {
                LOG.error("More unexpected exception, can not even write the error response about an internal error!", ex2);
            }
        } finally {
            if (!keepResponseOpen) {
                httpExchange.close();
            }
        }
    }

}
