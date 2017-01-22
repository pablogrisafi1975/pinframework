package com.pinframework.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderFile;

public class PinResponseOkFile extends PinResponse {

	public PinResponseOkFile(InputStream inputStream, String fileName, boolean download) {
		super(HttpURLConnection.HTTP_OK, inputStream, new PinRenderFile(fileName, download));
	}

	public PinResponseOkFile(String text, String fileName, boolean download) {
		super(HttpURLConnection.HTTP_OK, new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
				new PinRenderFile(fileName, download));
	}
}
