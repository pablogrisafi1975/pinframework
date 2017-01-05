package com.pinframework.upload;

import com.pinframework.exception.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public interface PinMutipartParamsParser {

	MultipartParams parse(HttpExchange httpExchange);

	static PinMutipartParamsParser createImpl() {
		
		try {
			return (PinMutipartParamsParser) Class.forName("com.pinframework.upload.PinMutipartParamsParserImpl").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new PinInitializationException(e);
		}
	}

}