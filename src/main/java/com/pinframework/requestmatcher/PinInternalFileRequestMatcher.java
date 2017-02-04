package com.pinframework.requestmatcher;

import com.pinframework.PinRequestMatcher;
import com.pinframework.PinUtils;

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
  public boolean matches(String method, String route, String accept) {
    if (!"GET".equals(method)) {
      LOG.error("Error trying to access '{}', wrong method '{}'", route, method);
      return false;
    }
    String fileName = parseFileName(route);

    return PinUtils.getResourceAsStream("static/" + fileName) != null;

  }

  private String parseFileName(String route) {
    String fileName = route.substring(appContext.length());
    // substring never returns null
    return fileName.length() == 0 ? "index.html" : fileName;
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
