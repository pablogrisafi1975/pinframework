package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderTextUtf8;

public class PinResponseOkText extends PinResponse {

	public PinResponseOkText(String text) {
		super(HttpURLConnection.HTTP_OK, text, PinRenderTextUtf8.INSTANCE);
	}
}
