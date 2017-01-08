package com.pinframework.handler;

import java.io.InputStream;

import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinResponse;
import com.pinframework.PinResponses;
import com.pinframework.PinServer;
import com.pinframework.requestmatcher.PinInternalFileRequestMatcher;

public class PinInternalFileHandler implements PinHandler {

	@Override
	public PinResponse handle(PinExchange pinExchange) throws Exception {
		String fileName = pinExchange.getPathParams().get(PinInternalFileRequestMatcher.FILE_NAME);
		String internalResourceName = pinExchange.getPathParams().get(PinInternalFileRequestMatcher.INTERNAL_RESOURCE_NAME);
		InputStream inputStream = PinServer.class.getClassLoader().getResourceAsStream(internalResourceName);
		return PinResponses.okFile(inputStream, fileName);
	}

}
