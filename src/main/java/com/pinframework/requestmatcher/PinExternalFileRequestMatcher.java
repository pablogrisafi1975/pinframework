package com.pinframework.requestmatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pinframework.PinRequestMatcher;
import com.pinframework.exception.PinIORuntimeException;

public class PinExternalFileRequestMatcher implements PinRequestMatcher {

	private static final Logger LOG = LoggerFactory.getLogger(PinExternalFileRequestMatcher.class);

	public static final String FILE_NAME = "FILE_NAME";
	public static final String EXTERNAL_FILE_NAME = "EXTERNAL_FILE_NAME";

	private final String appContext;

	private final File externalFolder;

	public PinExternalFileRequestMatcher(String appContext, File externalFolder) {
		this.appContext = appContext;
		this.externalFolder = externalFolder;
	}

	@Override
	public boolean matches(String method, String route, String contentType) {
		if (externalFolder == null) {
			return false;
		}
		if (!"GET".equals(method)) {
			LOG.error("Error trying to access '{}', wrong method '{}'", route, method);
			return false;
		}
		String fileName = parseFileName(route);

		File file;
		try {
			file = new File(externalFolder, fileName).getCanonicalFile();
		} catch (IOException e) {
			throw new PinIORuntimeException(e);
		}
		if (file.getAbsolutePath().indexOf(externalFolder.getAbsolutePath()) != 0) {
			LOG.error("Error trying to access '{}', directory traversal attack", route);
			return false;
		}
		return file.exists();
	}

	private String parseFileName(String route) {
		String filenameAux = route.substring(appContext.length());
		return filenameAux == null || filenameAux.length() == 0 ? "index.html" : filenameAux;
	}

	@Override
	public Map<String, String> extractPathParams(String route) {
		Map<String, String> map = new HashMap<>();
		String fileName = parseFileName(route);
		map.put(FILE_NAME, fileName);
		map.put(EXTERNAL_FILE_NAME, new File(externalFolder, fileName).getAbsolutePath());
		return map;
	}

}
