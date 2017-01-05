package com.pinframework;

import java.io.InputStream;

import com.pinframework.response.PinResponseNotFoundJson;
import com.pinframework.response.PinResponseNotFoundText;
import com.pinframework.response.PinResponseOkDownload;
import com.pinframework.response.PinResponseOkJson;
import com.pinframework.response.PinResponseOkText;

public final class PinResponses {
	public static final PinResponse okText(String text) {
		return PinResponseOkText.of(text);
	}

	public static final PinResponse okJson(Object obj) {
		return PinResponseOkJson.of(obj);
	}

	public static final PinResponse okDownload(InputStream inputStream, String fileName) {
		return PinResponseOkDownload.of(inputStream, fileName);
	}
	public static final PinResponse okDownload(String text, String fileName) {
		return PinResponseOkDownload.of(text, fileName);
	}

	public static final PinResponse notFoundText(String text) {
		return PinResponseNotFoundText.of(text);
	}

	public static final PinResponse notFoundJson(Object obj) {
		return PinResponseNotFoundJson.of(obj);
	}
}