package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderTextUtf8;

public class PinResponseNotFoundText extends PinResponse {

	public PinResponseNotFoundText(String text) {
		super(HttpURLConnection.HTTP_NOT_FOUND, text, PinRenderTextUtf8.INSTANCE);
	}
}