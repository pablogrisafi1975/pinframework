package com.pinframework;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface PinRequestPredicate {
	boolean test(String verb, String route, Map<String, List<String>> headers);
}
