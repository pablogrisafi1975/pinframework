package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderJsut;

public class PinResponseOkJsut extends PinResponse {

	private PinResponseOkJsut(Object obj, String template) {
		super(HttpURLConnection.HTTP_OK, new PinRenderJsut.Input(obj, template), PinRenderJsut.INSTANCE);
	}

	public static PinResponseOkJsut of(Object obj, String template) {
		return new PinResponseOkJsut(obj, template);
	}

}
