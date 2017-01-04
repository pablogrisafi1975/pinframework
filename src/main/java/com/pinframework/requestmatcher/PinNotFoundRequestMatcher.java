package com.pinframework.requestmatcher;

import java.util.Collections;
import java.util.Map;

import com.pinframework.PinRequestMatcher;

public class PinNotFoundRequestMatcher implements PinRequestMatcher {

	@Override
	public boolean matches(String verb, String route, String contentType) {
		return true;
	}

	@Override
	public Map<String, String> extractPathParams(String route) {
		return Collections.emptyMap();
	}

}
