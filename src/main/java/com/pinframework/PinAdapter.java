package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PinAdapter implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PinAdapter.class);

    static class PinHandlerRenderParamNames {
        private final PinHandler pinHandler;
        private final PinRender pinRender;
        private final List<String> parameterNames;

        public PinHandlerRenderParamNames(PinHandler pinHandler, PinRender pinRender, List<String> parameterNames) {
            this.pinHandler = pinHandler;
            this.parameterNames = parameterNames;
            this.pinRender = pinRender;
        }

        public PinHandler getPinHandler() {
            return pinHandler;
        }

        public PinRender getPinRender() {
            return pinRender;
        }

        public List<String> getParameterNames() {
            return parameterNames;
        }
    }

    private final Map<String, Map<String, PinHandlerRenderParamNames>> handlerByMethodAndPath = new HashMap<>();

    private final Gson gson;

    public PinAdapter(String method, String fullPath, List<String> pathParameterNames, PinHandler pinHandler, PinRender pinRender, Gson gson) {
        Map<String, PinHandlerRenderParamNames> map = new HashMap<>();
        map.put(fullPath, new PinHandlerRenderParamNames(pinHandler, pinRender, pathParameterNames));
        handlerByMethodAndPath.put(method, map);
        this.gson = gson;
    }

    public void put(String method, String fullPath, List<String> pathParameterNames, PinHandler pinHandler, PinRender pinRender) {
        if (handlerByMethodAndPath.containsKey(method)) {
            var map = handlerByMethodAndPath.get(method);
            if (map.containsKey(fullPath)) {
                throw new PinInitializationException(
                        "PinHandler already present method = '" + method + "' and full path = '" + fullPath + "'");
            } else {
                map.put(fullPath, new PinHandlerRenderParamNames(pinHandler, pinRender, pathParameterNames));
            }
        } else {
            Map<String, PinHandlerRenderParamNames> map = new HashMap<>();
            map.put(fullPath, new PinHandlerRenderParamNames(pinHandler, pinRender, pathParameterNames));
            handlerByMethodAndPath.put(method, map);
        }

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        Map<String, PinHandlerRenderParamNames> pinHandlerMap = handlerByMethodAndPath.get(method);
        if (pinHandlerMap == null) {
            LOG.error("Error trying to access '{}', wrong method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinHandlerRenderParamNames pinHandlerRenderParamNames = findMatchingPinHandler(httpExchange.getRequestURI().getPath(), pinHandlerMap);
        if (pinHandlerRenderParamNames == null) {
            LOG.error("No handler found for '{}' and method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinExchange pinExchange = new PinExchange(httpExchange, pinHandlerRenderParamNames.getParameterNames(), gson);
        PinRender pinRender = pinHandlerRenderParamNames.getPinRender();
        boolean keepResponseOpen = false;
        try {
            PinResponse pinResponse = pinHandlerRenderParamNames.getPinHandler().handle(pinExchange);
            pinRender.changeHeaders(httpExchange.getResponseHeaders());
            keepResponseOpen = pinResponse.keepResponseOpen();
            if (!keepResponseOpen) {
                httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
            }
            pinRender.render(pinResponse.getObj(), httpExchange.getResponseBody());
        }catch (PinBadRequestException bre){
            // this is an expected behaviour , so no error
            try {
                pinRender.changeHeaders(httpExchange.getResponseHeaders());
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
                pinRender.render(bre, httpExchange.getResponseBody());
            } catch (Exception ex2) {
                LOG.error("Unexpected exception, can not write the response of a bad request", ex2);
            }

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

    private PinHandlerRenderParamNames findMatchingPinHandler(String requestURIPath, Map<String, PinHandlerRenderParamNames> pinHandlerMap) {

        var matchingMap = pinHandlerMap.entrySet().stream().filter(e -> uriMatches(e.getKey(), requestURIPath))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (matchingMap.isEmpty()) {
            LOG.error("Zero handler found for requestURI.path {}: {}", requestURIPath, pinHandlerMap.keySet());
        }
        if (matchingMap.size() == 1) {
            return matchingMap.values().iterator().next();
        }
        LOG.error("More than one handler for requestURI.path {}: {}", requestURIPath, pinHandlerMap.keySet());
        return null;
    }

    private boolean uriMatches(String key, String requestURIPath) {
        //key and requestURIPath starts with /, substring to ignore it
        //key and/or requestURIPath mey end with /, PinUtils to remove
        var keyParts = PinUtils.removeTrailingSlash(key.substring(1)).split("/", -1);
        var pathParts = PinUtils.removeTrailingSlash(requestURIPath.substring(1)).split("/", -1);
        if (keyParts.length != pathParts.length) {
            return false;
        }
        for (var i = 0; i < keyParts.length; i++) {
            String keyPart = keyParts[i];
            String pathPart = pathParts[i];
            if(keyPart.charAt(0) != ':' && !keyPart.equals(pathPart)){
                return false;
            }
        }
        return true;

    }

}
