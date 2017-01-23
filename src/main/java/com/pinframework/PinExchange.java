package com.pinframework;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.pinframework.upload.FileParam;
import com.sun.net.httpserver.HttpExchange;

public class PinExchange {


	private final HttpExchange httpExchange;
	private Map<String, List<String>> queryParams;
	private Map<String, Object> postParams;
	private Map<String, String> pathParams;
	private Map<String, FileParam> fileParams;

	public PinExchange(HttpExchange httpExchange, Map<String, String> pathParams, Map<String, List<String>> queryParams,
			Map<String, Object> postParams, Map<String, FileParam> fileParams) {
		this.httpExchange = httpExchange;
		this.pathParams = Collections.unmodifiableMap(pathParams);
		this.queryParams = Collections.unmodifiableMap(queryParams);
		this.postParams = Collections.unmodifiableMap(postParams);
		this.fileParams = Collections.unmodifiableMap(fileParams);
	}



	public HttpExchange raw() {
		return httpExchange;
	}

	public Map<String, List<String>> getQueryParams() {
		return queryParams;
	}

	public Map<String, String> getPathParams() {
		return pathParams;
	}

	/**
	 * Only actual files are returned. Extra values are present in getPostParams
	 * 
	 * @return
	 */
	public Map<String, FileParam> getFileParams() {
		return fileParams;
	}



	public Map<String, Object> getPostParams() {
		return postParams;
	}


	public Map<String, List<String>> getRequestHeaders() {
		// only because headers class has restrictions
		return httpExchange.getRequestHeaders();
	}

	public Map<String, List<String>> getResponseHeaders() {
		// only because headers class has restrictions
		return httpExchange.getResponseHeaders();
	}

}
