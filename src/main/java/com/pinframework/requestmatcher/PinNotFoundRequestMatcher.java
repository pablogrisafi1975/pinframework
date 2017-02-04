package com.pinframework.requestmatcher;

import com.pinframework.PinRequestMatcher;

import java.util.Collections;
import java.util.Map;

public class PinNotFoundRequestMatcher implements PinRequestMatcher {

  @Override
  public boolean matches(String verb, String route, String accept) {
    return true;
  }

  @Override
  public Map<String, String> extractPathParams(String route) {
    return Collections.emptyMap();
  }

}
