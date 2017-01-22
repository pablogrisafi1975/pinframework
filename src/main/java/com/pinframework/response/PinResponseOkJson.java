package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJson;

public class PinResponseOkJson extends PinResponse {

	public PinResponseOkJson(Object obj) {
		super(HttpURLConnection.HTTP_OK, obj, PinRenderJson.INSTANCE);
	}
}
