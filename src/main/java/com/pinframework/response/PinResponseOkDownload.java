package com.pinframework.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderFileDownload;

public class PinResponseOkDownload extends PinResponse {

	private PinResponseOkDownload(InputStream inputStream, String fileName) {
		super(HttpURLConnection.HTTP_OK, inputStream, new PinRenderFileDownload(fileName));
	}

	public static PinResponseOkDownload of(InputStream inputStream, String fileName) {
		return new PinResponseOkDownload(inputStream, fileName);
	}
	public static PinResponseOkDownload of(String text, String fileName) {
		return new PinResponseOkDownload(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), fileName);
	}

}
