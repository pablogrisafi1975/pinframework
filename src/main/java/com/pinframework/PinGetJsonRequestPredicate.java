package com.pinframework;

import java.util.List;
import java.util.Map;

public class PinGetJsonRequestPredicate implements PinRequestPredicate {

	private String route;

	public PinGetJsonRequestPredicate(String route) {
		this.route = route;
	}

	@Override
	public boolean test(String verb, String route, Map<String, List<String>> headers) {
		return "GET".equals(verb) && route.equalsIgnoreCase(route);
	}

}
