package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pinframework.exceptions.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PinAdapter implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PinAdapter.class);

    static class PinHandlerWithParamNames{
        private final PinHandler pinHandler;
        private final List<String> parameterNames;

        public PinHandlerWithParamNames(PinHandler pinHandler, List<String> parameterNames) {
            this.pinHandler = pinHandler;
            this.parameterNames = parameterNames;
        }

        public PinHandler getPinHandler() {
            return pinHandler;
        }

        public List<String> getParameterNames() {
            return parameterNames;
        }
    }

    private final Map<String, Map<String, PinHandlerWithParamNames>> handlerByMethodAndPath = new HashMap<>();
    private final PinRender pinRender;

    //necesito tener mas de un handler por metodo y elegir en base a los path parameteres
    public PinAdapter(String method, String fullPath, List<String> pathParameterNames, PinHandler pinHandler, PinRender pinRender) {
        Map<String, PinHandlerWithParamNames> map = new HashMap<>();
        map.put(fullPath, new PinHandlerWithParamNames(pinHandler, pathParameterNames));
        handlerByMethodAndPath.put(method, map);
        this.pinRender = pinRender;
    }

    public void put(String method, String fullPath, List<String> pathParameterNames, PinHandler pinHandler) {
        if (handlerByMethodAndPath.containsKey(method)) {
            var map = handlerByMethodAndPath.get(method);
            if (map.containsKey(fullPath)) {
                throw new PinInitializationException(
                        "PinHandler already present method = '" + method + "' and full path = '" + fullPath + "'");
            } else {
                map.put(fullPath, new PinHandlerWithParamNames(pinHandler, pathParameterNames));
            }
        } else {
            Map<String, PinHandlerWithParamNames> map = new HashMap<>();
            map.put(fullPath, new PinHandlerWithParamNames(pinHandler, pathParameterNames));
            handlerByMethodAndPath.put(method, map);
        }

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        Map<String, PinHandlerWithParamNames> pinHandlerMap = handlerByMethodAndPath.get(method);
        if (pinHandlerMap == null) {
            LOG.error("Error trying to access '{}', wrong method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinHandlerWithParamNames pinHandlerWithParamNames = findMatchingPinHandler(httpExchange.getRequestURI().getPath(), pinHandlerMap);
        if (pinHandlerWithParamNames == null) {
            LOG.error("No handler found for '{}' and method '{}'", httpExchange.getRequestURI().getPath(),
                    method);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            //TODO: devolver mas info?
            httpExchange.close();
            return;
        }
        PinExchange pinExchange = new PinExchange(httpExchange, pinHandlerWithParamNames.getParameterNames());
        boolean keepResponseOpen = false;
        try {
            PinResponse pinResponse = pinHandlerWithParamNames.getPinHandler().handle(pinExchange);
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

    private PinHandlerWithParamNames findMatchingPinHandler(String requestURIPath, Map<String, PinHandlerWithParamNames> pinHandlerMap) {

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
