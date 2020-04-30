package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PinAdapter implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PinAdapter.class);

    private static class PinHandlerRender {
        private final PinHandler pinHandler;
        private final PinRender pinRender;

        public PinHandlerRender(PinHandler pinHandler, PinRender pinRender) {
            this.pinHandler = pinHandler;
            this.pinRender = pinRender;
        }
    }

    private final Map<String, Map<String, PinHandlerRender>> handlerByMethodAndPath = new HashMap<>();

    private final Gson gson;

    public PinAdapter(String method, String fullPath, PinHandler pinHandler, PinRender pinRender, Gson gson) {
        Map<String, PinHandlerRender> map = new HashMap<>();
        map.put(fullPath, new PinHandlerRender(pinHandler, pinRender));
        handlerByMethodAndPath.put(method, map);
        this.gson = gson;
    }

    public void put(String method, String fullPath, PinHandler pinHandler, PinRender pinRender) {
        if (handlerByMethodAndPath.containsKey(method)) {
            var map = handlerByMethodAndPath.get(method);
            if (map.containsKey(fullPath)) {
                throw new PinInitializationException(
                        "PinHandler already present method = '" + method + "' and full path = '" + fullPath + "'");
            } else {
                map.put(fullPath, new PinHandlerRender(pinHandler, pinRender));
            }
        } else {
            Map<String, PinHandlerRender> map = new HashMap<>();
            map.put(fullPath, new PinHandlerRender(pinHandler, pinRender));
            handlerByMethodAndPath.put(method, map);
        }

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        Map<String, PinHandlerRender> pinHandlerMap = handlerByMethodAndPath.get(method);
        if (pinHandlerMap == null) {
            LOG.error("Error trying to access '{}', wrong method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinHandlerRenderWithPathParams pinHandlerRenderWithPathParams = findMatchingPinHandler(httpExchange.getRequestURI().getPath(),
                pinHandlerMap);
        if (pinHandlerRenderWithPathParams == null) {
            LOG.error("No handler found for '{}' and method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinExchange pinExchange = new PinExchange(httpExchange, gson, pinHandlerRenderWithPathParams.pathParams);
        PinRender pinRender = pinHandlerRenderWithPathParams.pinHandlerRender.pinRender;
        boolean keepResponseOpen = false;
        try {
            PinResponse pinResponse = pinHandlerRenderWithPathParams.pinHandlerRender.pinHandler.handle(pinExchange);
            pinRender.changeHeaders(httpExchange.getResponseHeaders());
            keepResponseOpen = pinResponse.keepResponseOpen();
            if (!keepResponseOpen) {
                httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
            }
            pinRender.render(pinResponse.getObj(), httpExchange.getResponseBody());
        } catch (PinBadRequestException bre) {
            // this is an expected behaviour , so no error
            LOG.debug("Exception trying to read data", bre);
            try {
                pinRender.changeHeaders(httpExchange.getResponseHeaders());
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                pinRender.render(bre, httpExchange.getResponseBody());
            } catch (Exception ex2) {
                LOG.error("Unexpected exception, can not write the response of a bad request", ex2);
            }

        } catch (Exception ex) {
            LOG.error("Unexpected exception, will return HTTP_INTERNAL_ERROR = 500", ex);
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

    private static class PinHandlerRenderWithPathParams {
        private final PinHandlerRender pinHandlerRender;
        private final Map<String, String> pathParams;

        public PinHandlerRenderWithPathParams(PinHandlerRender pinHandlerRender, Map<String, String> pathParams) {
            this.pinHandlerRender = pinHandlerRender;
            this.pathParams = pathParams;
        }
    }

    private PinHandlerRenderWithPathParams findMatchingPinHandler(String requestURIPath, Map<String, PinHandlerRender> pinHandlerMap) {
        Map<String, PinHandlerRenderWithPathParams> matchingMap = new HashMap<>();
        for (var entry : pinHandlerMap.entrySet()) {
            String path = entry.getKey();
            PinHandlerRender pinHandlerRender = entry.getValue();
            UriMatchesWithPathParams uriMatchesWithPathParams = uriMatchesWithParams(path, requestURIPath);
            if (uriMatchesWithPathParams.matches) {
                matchingMap.put(path, new PinHandlerRenderWithPathParams(pinHandlerRender, uriMatchesWithPathParams.pathParams));
            }
        }

        if (matchingMap.isEmpty()) {
            LOG.error("Zero handler found for requestURI.path {}: {}", requestURIPath, pinHandlerMap.keySet());
        }
        if (matchingMap.size() == 1) {
            return matchingMap.values().iterator().next();
        }
        LOG.error("More than one handler for requestURI.path {}: {}", requestURIPath, matchingMap.entrySet());
        return null;
    }

    private static class UriMatchesWithPathParams {
        private final boolean matches;
        private final Map<String, String> pathParams;

        public UriMatchesWithPathParams(boolean matches, Map<String, String> pathParams) {
            this.matches = matches;
            this.pathParams = pathParams;
        }
    }

    private UriMatchesWithPathParams uriMatchesWithParams(String key, String requestURIPath) {
        //key and requestURIPath starts with /, substring to ignore it
        //key and/or requestURIPath may end with /, PinUtils to remove
        var keyParts = PinUtils.removeTrailingSlash(key.substring(1)).split("/", -1);
        var pathParts = PinUtils.removeTrailingSlash(requestURIPath.substring(1)).split("/", -1);
        if (keyParts.length != pathParts.length) {
            return new UriMatchesWithPathParams(false, null);
        }
        Map<String, String> pathParams = new HashMap<>();
        for (var i = 0; i < keyParts.length; i++) {
            String keyPart = keyParts[i];
            String pathPart = pathParts[i];
            char firstCharOfKey = keyPart.charAt(0);
            if (firstCharOfKey == ':') {
                //substring(1) to remove semicolon
                pathParams.put(keyPart.substring(1), pathPart);
            }
            if (firstCharOfKey != ':' && !keyPart.equals(pathPart)) {
                return new UriMatchesWithPathParams(false, null);
            }
        }
        return new UriMatchesWithPathParams(true, pathParams);

    }

}
