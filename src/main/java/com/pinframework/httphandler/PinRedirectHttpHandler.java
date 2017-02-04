package com.pinframework.httphandler;

import com.google.gson.Gson;
import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinParamsParser;
import com.pinframework.PinRender;
import com.pinframework.PinRequestMatcher;
import com.pinframework.PinResponse;
import com.pinframework.constant.PinHeader;
import com.pinframework.handler.PinExternalFileHandler;
import com.pinframework.handler.PinInternalFileHandler;
import com.pinframework.handler.PinNotFoundHandler;
import com.pinframework.requestmatcher.PinExternalFileRequestMatcher;
import com.pinframework.requestmatcher.PinInternalFileRequestMatcher;
import com.pinframework.requestmatcher.PinNotFoundRequestMatcher;
import com.pinframework.upload.FileParam;
import com.pinframework.upload.MultipartParams;
import com.pinframework.upload.PinMutipartParamsParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinRedirectHttpHandler implements HttpHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PinRedirectHttpHandler.class);


  private final Map<PinRequestMatcher, PinHandler> routeMap =
      Collections.synchronizedMap(new LinkedHashMap<>());

  private final PinRequestMatcher notFoundRequestMatcher = new PinNotFoundRequestMatcher();

  private final PinHandler notFoundHandler = new PinNotFoundHandler();

  private final PinRequestMatcher externalFileRequestMatcher;

  private final PinHandler externalFileHandler = new PinExternalFileHandler();

  private final PinRequestMatcher internalFileRequestMatcher;

  private final PinHandler internalFileHandler = new PinInternalFileHandler();

  private final PinParamsParser paramsParser;

  private final PinMutipartParamsParser multipartParamsParser;

  private final boolean uploadSupportEnabled;


  public PinRedirectHttpHandler(String appContext, File externalFolder, Gson gsonParser,
      boolean uploadSupportEnabled) {
    this.uploadSupportEnabled = uploadSupportEnabled;
    this.paramsParser = new PinParamsParser(gsonParser);
    this.multipartParamsParser = uploadSupportEnabled ? PinMutipartParamsParser.createImpl() : null;
    this.internalFileRequestMatcher = new PinInternalFileRequestMatcher(appContext);
    this.externalFileRequestMatcher = new PinExternalFileRequestMatcher(appContext, externalFolder);
  }

  public void on(PinRequestMatcher requestPredicate, PinHandler handler) {
    routeMap.put(requestPredicate, handler);
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    String route = httpExchange.getRequestURI().getPath();
    String method = httpExchange.getRequestMethod();
    String accept = httpExchange.getRequestHeaders().getFirst(PinHeader.ACCEPT);

    /* first try services */
    for (Map.Entry<PinRequestMatcher, PinHandler> entry : routeMap.entrySet()) {
      PinRequestMatcher requestMatcher = entry.getKey();
      if (requestMatcher.matches(method, route, accept)) {
        PinHandler pinHandler = entry.getValue();
        process(route, httpExchange, requestMatcher, pinHandler);
        return;
      }
    }
    /* then external files */
    if (externalFileRequestMatcher.matches(method, route, accept)) {
      process(route, httpExchange, externalFileRequestMatcher, externalFileHandler);
      return;
    }

    /* then internal files */
    if (internalFileRequestMatcher.matches(method, route, accept)) {
      process(route, httpExchange, internalFileRequestMatcher, internalFileHandler);
      return;
    }

    /* then not found */
    process(route, httpExchange, notFoundRequestMatcher, notFoundHandler);
  }

  private void process(String route, HttpExchange httpExchange, PinRequestMatcher requestMatcher,
      PinHandler pinHandler) {
    Map<String, String> pathParams = requestMatcher.extractPathParams(route);

    Map<String, List<String>> queryParams =
        paramsParser.queryParams(httpExchange.getRequestURI().getRawQuery());

    String fullContentType = httpExchange.getRequestHeaders().getFirst(PinHeader.CONTENT_TYPE);

    Map<String, Object> bodyParams = null;
    Map<String, FileParam> fileParams = Collections.emptyMap();
    if (paramsParser.isMultipart(fullContentType)) {
      if (uploadSupportEnabled) {
        MultipartParams multipartParams = multipartParamsParser.parse(httpExchange);
        bodyParams = multipartParams.bodyParams();
        fileParams = multipartParams.fileParams();
      } else {
        LOG.error("Trying to upload but uploadSupportEnabled is false");
      }
    } else {
      bodyParams = paramsParser.bodyParams(fullContentType, httpExchange.getRequestBody());
    }

    PinExchange pinExchange =
        new PinExchange(httpExchange, pathParams, queryParams, bodyParams, fileParams);
    boolean keepResponseOpen = false;
    try {
      PinResponse pinResponse = pinHandler.handle(pinExchange);
      PinRender pinTransformer = pinResponse.getRender();
      pinTransformer.changeHeaders(httpExchange.getResponseHeaders());
      keepResponseOpen = pinResponse.keepOpen();
      if (!keepResponseOpen) {
        httpExchange.sendResponseHeaders(pinResponse.getStatus(), 0);
      }
      pinTransformer.render(pinResponse.getObj(), httpExchange.getResponseBody());
    } catch (Exception ex) {
      // TODO usar los exception handlers, tener en cuenta o no las PinExceptions?
      // TODO: before, after, onexception, onsuccess
      ex.printStackTrace();
    } finally {
      if (!keepResponseOpen) {
        httpExchange.close();
      }
    }
  }

}
