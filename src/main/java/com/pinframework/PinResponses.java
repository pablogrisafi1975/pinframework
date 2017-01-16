package com.pinframework;

import java.io.InputStream;

import com.pinframework.response.PinResponseNotFoundJson;
import com.pinframework.response.PinResponseNotFoundText;
import com.pinframework.response.PinResponseOkFile;
import com.pinframework.response.PinResponseOkJson;
import com.pinframework.response.PinResponseOkText;
import com.pinframework.response.PinResponseOkJsut;

public final class PinResponses {
	public static final PinResponse okText(String text) {
		return PinResponseOkText.of(text);
	}

	public static final PinResponse okJson(Object obj) {
		return PinResponseOkJson.of(obj);
	}

	public static final PinResponse okJsut(Object obj, String template) {
		return PinResponseOkJsut.of(obj, template);
	}

	public static final PinResponse okDownload(InputStream inputStream, String fileName) {
		return PinResponseOkFile.of(inputStream, fileName, true);
	}
	public static final PinResponse okDownload(String text, String fileName) {
		return PinResponseOkFile.of(text, fileName, true);
	}

	public static final PinResponse notFoundText(String text) {
		return PinResponseNotFoundText.of(text);
	}

	public static final PinResponse notFoundJson(Object obj) {
		return PinResponseNotFoundJson.of(obj);
	}

	public static PinResponse okFile(InputStream inputStream, String filename) {
		return PinResponseOkFile.of(inputStream, filename, false);
	}
}
