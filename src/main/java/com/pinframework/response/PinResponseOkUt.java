package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderUt;

public class PinResponseOkUt extends PinResponse {

	private PinResponseOkUt(Object obj, String template) {
		//TODO: cache templates
		super(HttpURLConnection.HTTP_OK, obj, new PinRenderUt(template));
	}

	public static PinResponseOkUt of(Object obj, String template) {
		return new PinResponseOkUt(obj, template);
	}

}
