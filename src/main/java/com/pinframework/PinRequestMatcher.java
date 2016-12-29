package com.pinframework;

import java.util.Map;

public interface PinRequestMatcher {
	boolean matches(String verb, String route, String contentType);
	Map<String, String> extractPathParams(String route);
}
