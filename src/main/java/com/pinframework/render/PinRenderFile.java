package com.pinframework.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinUtils;

public class PinRenderFile implements PinRender {

	private final String fileName;

	public PinRenderFile(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public void render(Object obj, OutputStream outputStream) throws IOException {

		PinUtils.copy((InputStream) obj, outputStream);

	}

	@Override
	public void changeHeaders(Map<String, List<String>> responseHeaders) {
		String mimeType = PinMimeType.fromFileName(fileName);
		PinUtils.put(responseHeaders, PinMimeType.CONTENT_TYPE, mimeType);

	}

}
