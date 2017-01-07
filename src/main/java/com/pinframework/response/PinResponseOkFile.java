package com.pinframework.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinResponse;
import com.pinframework.render.PinRenderFile;

public class PinResponseOkFile extends PinResponse {

	private PinResponseOkFile(InputStream inputStream, String fileName, boolean download) {
		super(HttpURLConnection.HTTP_OK, inputStream, new PinRenderFile(fileName, download));
	}

	public static PinResponseOkFile of(InputStream inputStream, String fileName, boolean download) {
		return new PinResponseOkFile(inputStream, fileName, download);
	}
	
	public static PinResponseOkFile of(String text, String fileName, boolean download) {
		return new PinResponseOkFile(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), fileName, download);
	}
	

}
