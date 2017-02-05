package com.pinframework.upload;

import java.util.Map;

public class PinMultipartParams {

  private final Map<String, PinFileParam> fileParams;
  private final Map<String, Object> bodyParams;

  public PinMultipartParams(Map<String, PinFileParam> fileParams, Map<String, Object> bodyParams) {
    this.fileParams = fileParams;
    this.bodyParams = bodyParams;
  }

  public Map<String, PinFileParam> fileParams() {
    return fileParams;
  }

  public Map<String, Object> bodyParams() {
    return bodyParams;
  }



}
