package com.pinframework;

import java.util.Map;

public interface PinRequestMatcher {

  boolean matches(String method, String route, String accept);

  Map<String, String> extractPathParams(String route);
}
