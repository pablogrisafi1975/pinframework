package com.pinframework.upload;

import java.util.Map;

public class MultipartParams {

	private final Map<String, FileParam> fileParams;
	private final Map<String, Object> postParams;

	public MultipartParams(Map<String, FileParam> fileParams, Map<String, Object> postParams) {
		this.fileParams = fileParams;
		this.postParams = postParams;
	}

	public Map<String, FileParam> getFileParams() {
		return fileParams;
	}

	public Map<String, Object> getPostParams() {
		return postParams;
	}
	
	

}
