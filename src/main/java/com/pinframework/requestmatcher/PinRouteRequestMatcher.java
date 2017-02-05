package com.pinframework.requestmatcher;

import com.pinframework.PinRequestMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PinRouteRequestMatcher implements PinRequestMatcher {

  private final String method;
  private final String appContext;
  private final String route;
  private final Pattern routePattern;
  private final Pattern capturePattern;
  private final List<String> acceptList;
  private final List<String> parameterNameList = new ArrayList<>();

  public PinRouteRequestMatcher(String method, String route, String appContext, String... accept) {
    this.method = method;
    this.route = route;
    this.appContext = appContext;
    // TODO: validar que los nombres sole tengan AZaz09 y empiecen con
    // letras, ver
    // http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#groupname
    String routeRegex = appContext + route.replaceAll("/\\:([a-zA-Z][a-zA-Z0-9\\-]*)", "/.+");
    routePattern = Pattern.compile(routeRegex);
    Matcher matcher = Pattern.compile("/\\:([a-zA-Z][a-zA-Z0-9\\-]*)").matcher(route);
    while (matcher.find()) {
      String parameterName = matcher.group(1);
      parameterNameList.add(parameterName);
      // TODO:validar que no se repitan
    }
    String captureRegex =
        appContext + route.replaceAll("/\\:([a-zA-Z][a-zA-Z0-9\\-]*)", "/([^/]+)");
    capturePattern = Pattern.compile(captureRegex);
    this.acceptList = Collections.unmodifiableList(
        Arrays.asList(accept).stream().map(String::toLowerCase).collect(Collectors.toList()));
  }

  @Override
  public boolean matches(String verb, String route, String contentType) {
    if (!acceptList.isEmpty() && contentType != null
        && !acceptList.contains(contentType.toLowerCase())) {
      return false;
    }
    return Objects.equals(verb, method) && this.routePattern.matcher(route).matches();
  }

  @Override
  public Map<String, String> extractPathParams(String route) {
    Map<String, String> map = new HashMap<>();
    Matcher matcher = capturePattern.matcher(route);
    if (matcher.matches()) {
      for (int i = 0; i < parameterNameList.size(); i++) {
        String parameterValue = matcher.group(i + 1);
        map.put(parameterNameList.get(i), parameterValue);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    String contentType =
        this.acceptList.isEmpty() ? "Accept:Anything" : "Accept:" + String.join(", ", acceptList);
    String paramList = this.parameterNameList.isEmpty() ? "No params"
        : "Params: " + parameterNameList.stream().collect(Collectors.joining(","));
    return "[" + this.method + "]" + this.appContext + this.route + " (" + contentType + ") ("
        + paramList + ")";
  }

}
