package com.pinframework;

import java.io.InputStream;

import com.pinframework.response.PinResponseNotFoundJson;
import com.pinframework.response.PinResponseNotFoundText;
import com.pinframework.response.PinResponseOkFile;
import com.pinframework.response.PinResponseOkJson;
import com.pinframework.response.PinResponseOkText;
import com.pinframework.response.PinResponseSse;
import com.pinframework.response.PinResponseOkJsut;

public final class PinResponses {
	
	private PinResponses (){
		//do nothing, shut up sonar
	}
	public static PinResponse okText(String text) {
		return new PinResponseOkText(text);
	}

	public static PinResponse okJson(Object obj) {
		return new PinResponseOkJson(obj);
	}

	public static PinResponse okJsut(Object obj, String template) {
		return new PinResponseOkJsut(obj, template);
	}

	public static PinResponse okDownload(InputStream inputStream, String fileName) {
		return new PinResponseOkFile(inputStream, fileName, true);
	}
	public static PinResponse okDownload(String text, String fileName) {
		return new PinResponseOkFile(text, fileName, true);
	}

	public static PinResponse notFoundText(String text) {
		return new PinResponseNotFoundText(text);
	}

	public static PinResponse notFoundJson(Object obj) {
		return new PinResponseNotFoundJson(obj);
	}

	public static PinResponse okFile(InputStream inputStream, String filename) {
		return new PinResponseOkFile(inputStream, filename, false);
	}

	public static PinResponseSse okSse(PinExchange pex) {
		return new PinResponseSse(pex);
	}
}
