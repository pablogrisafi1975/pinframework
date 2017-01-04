package com.pinframework.requestmatcher;

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

import com.pinframework.PinRequestMatcher;

public class PinRouteRequestMatcher implements PinRequestMatcher {

	private final String method;
	private final Pattern routePattern;
	private final Pattern capturePattern;
	private final List<String> contentTypeList;
	private final List<String> parameterNameList = new ArrayList<>();

	public PinRouteRequestMatcher(String method, String route, String appContext, String... contentType) {
		this.method = method;
		// TODO: validar que los nombres sole tengan AZaz09 y empiecen con
		// letras, ver
		// http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#groupname
		String routeRegex = appContext + route.replaceAll("/\\:[a-zA-Z0-9]+", "/.+");
		routePattern = Pattern.compile(routeRegex);
		Matcher matcher = Pattern.compile("/\\:([a-zA-Z0-9]+)").matcher(route);
		while (matcher.find()) {
			String parameterName = matcher.group(1);
			parameterNameList.add(parameterName);
			// TODO:validar que no se repitan
		}
		String captureRegex = appContext + route.replaceAll("/\\:([a-zA-Z0-9]+)", "/([^/]+)");
		capturePattern = Pattern.compile(captureRegex);
		this.contentTypeList = Collections.unmodifiableList(
				Arrays.asList(contentType).stream().map(String::toUpperCase).collect(Collectors.toList()));
	}

	@Override
	public boolean matches(String verb, String route, String contentType) {
		if (!contentTypeList.isEmpty() && contentType != null && !contentTypeList.contains(contentType.toUpperCase())) {
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

}
