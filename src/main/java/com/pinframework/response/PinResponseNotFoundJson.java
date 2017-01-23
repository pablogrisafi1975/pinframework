package com.pinframework.response;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJson;

import java.net.HttpURLConnection;

public class PinResponseNotFoundJson extends PinResponse {

	public PinResponseNotFoundJson(Object obj) {
		super(HttpURLConnection.HTTP_NOT_FOUND, obj, PinRenderJson.INSTANCE);
	}
}
