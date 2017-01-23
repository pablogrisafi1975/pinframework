package com.pinframework.requestmatcher;

import com.pinframework.PinRequestMatcher;
import com.pinframework.PinServer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinInternalFileRequestMatcher implements PinRequestMatcher {

  private static final Logger LOG = LoggerFactory.getLogger(PinInternalFileRequestMatcher.class);

  public static final String INTERNAL_RESOURCE_NAME = "INTERNAL_RESOURCE_NAME";
  public static final String FILE_NAME = "FILE_NAME";

  private final String appContext;

  public PinInternalFileRequestMatcher(String appContext) {
    this.appContext = appContext;
  }

  @Override
  public boolean matches(String method, String route, String contentType) {
    if (!"GET".equals(method)) {
      LOG.error("Error trying to access '{}', wrong method '{}'", route, method);
      return false;
    }
    String fileName = parseFileName(route);

    return PinServer.class.getClassLoader().getResourceAsStream("static/" + fileName) != null;

  }

  private String parseFileName(String route) {
    String filenameAux = route.substring(appContext.length());
    return filenameAux == null || filenameAux.length() == 0 ? "index.html" : filenameAux;
  }

  @Override
  public Map<String, String> extractPathParams(String route) {
    Map<String, String> map = new HashMap<>();
    String fileName = parseFileName(route);
    map.put(FILE_NAME, fileName);
    map.put(INTERNAL_RESOURCE_NAME, "static/" + fileName);
    return map;
  }

}
