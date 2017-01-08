package com.pinframework.handler;

import java.util.HashMap;
import java.util.Map;

import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinMimeType;
import com.pinframework.PinResponse;
import com.pinframework.PinResponses;
import com.pinframework.PinUtils;

public class PinNotFoundHandler implements PinHandler {

	@SuppressWarnings("restriction")
	@Override
	public PinResponse handle(PinExchange pinExchange) throws Exception {
		String contentType = PinUtils.getFirst(pinExchange.getRequestHeaders(), "contentType");
		String requestURI = pinExchange.raw().getRequestURI().normalize().toString();
		if(PinMimeType.APPLICATION_JSON_UTF8.equals(contentType) || PinMimeType.APPLICATION_JSON.equals(contentType)){
			Map<String, String> map = new HashMap<>();
			map.put("requestUri", requestURI);
			return PinResponses.notFoundJson(map);
		}
		String text = "Can not find " + requestURI;
		return PinResponses.notFoundText(text);
	}

}
