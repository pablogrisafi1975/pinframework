package com.pinframework.response;

import java.io.InputStream;
import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderFile;

public class PinResponseOkFile extends PinResponse {

	private PinResponseOkFile(InputStream inputStream, String fileName) {
		super(HttpURLConnection.HTTP_OK, inputStream, new PinRenderFile(fileName));
	}

	public static PinResponseOkFile of(InputStream inputStream, String fileName) {
		return new PinResponseOkFile(inputStream, fileName);
	}
	

}
