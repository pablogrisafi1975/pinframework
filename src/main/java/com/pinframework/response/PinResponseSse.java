package com.pinframework.response;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinContentType;
import com.pinframework.PinExchange;
import com.pinframework.PinGson;
import com.pinframework.PinResponse;
import com.pinframework.PinUtils;
import com.pinframework.exception.PinIORuntimeException;
import com.pinframework.render.PinRenderNull;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class PinResponseSse extends PinResponse {
	private final PrintWriter printWriter;
	private final PinExchange pinExchange;

	private PinResponseSse(PinExchange pinExchange) {
		super(HttpURLConnection.HTTP_OK, pinExchange, PinRenderNull.INSTANCE);
		this.pinExchange = pinExchange;
		HttpExchange httpExchange = pinExchange.raw();
		httpExchange.getResponseHeaders().add(PinContentType.CONTENT_TYPE, PinContentType.TEXT_EVENT_STREAM);
		httpExchange.getResponseHeaders().add("CharacterEncoding", "UTF-8");
		httpExchange.getResponseHeaders().add("cache-control", "no-cache");
		httpExchange.getResponseHeaders().add("connection", "keep-alive");
		try {
			httpExchange.sendResponseHeaders(200, 0);
			httpExchange.getResponseBody().flush();
			PinUtils.fullyRead(httpExchange.getRequestBody());
		} catch (IOException e) {
			throw new PinIORuntimeException(e);
		}
		this.printWriter = new PrintWriter(
				new OutputStreamWriter(httpExchange.getResponseBody(), StandardCharsets.UTF_8), false);
	}

	public static PinResponseSse of(PinExchange pinExchange) {
		return new PinResponseSse(pinExchange);
	}

	/**
	 * @param data
	 * @return true is sent without errors, false if couldn't send data
	 */
	public boolean sendObject(Object data) {
		String jsonData = PinGson.getInstance().toJson(data);
		return send(jsonData);
	}

	public boolean sendObject(String event, Object data) {
		String jsonData = PinGson.getInstance().toJson(data);
		return send(event, jsonData);
	}

	public boolean send(String data) {
		return send(null, data);
	}

	/**
	 * @param data
	 * @return true is sent without errors, false if couldn't send data
	 */
	public boolean send(String event, String data) {
		if (event != null) {
			String next = "event: " + event + "\n";
			printWriter.append(next);
			if (printWriter.checkError()) {
				return false;
			}
		}
		// TODO: keep alive, id on retry
		String[] lines = data.split("\n", -1);
		for (String line : lines) {
			String next = "data: " + line + "\n";
			printWriter.append(next);
			if (printWriter.checkError()) {
				return false;
			}
		}
		printWriter.append("\n");
		return !printWriter.checkError();
	}

	@Override
	public boolean keepResponseOpen() {
		return true;
	}

	public boolean comment(String comment) {
		String next = ": " + comment + "\n\n";
		printWriter.append(next);
		return !printWriter.checkError();

	}

	public void close() {
		try {
			pinExchange.raw().getResponseBody().close();
		} catch (IOException e) {
			throw new PinIORuntimeException(e);
		}
	}

}
