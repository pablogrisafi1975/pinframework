package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJsut;

public class PinResponseOkJsut extends PinResponse {

	public PinResponseOkJsut(Object obj, String template) {
		super(HttpURLConnection.HTTP_OK, new PinRenderJsut.Input(obj, template), PinRenderJsut.INSTANCE);
	}
}
