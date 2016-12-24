package com.pinframework;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class PinRedirectHandler implements HttpHandler {
	public void on(PinRequestPredicate requestPredicate, PinHandler handler){
		
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

	}

}
