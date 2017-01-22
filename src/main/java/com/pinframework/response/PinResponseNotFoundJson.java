package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJson;

public class PinResponseNotFoundJson extends PinResponse {

	public PinResponseNotFoundJson(Object obj) {
		super(HttpURLConnection.HTTP_NOT_FOUND, obj, PinRenderJson.INSTANCE);
	}
}
