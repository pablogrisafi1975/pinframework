package com.pinframework.upload;

import java.util.Map;

public class MultipartParams {

  private final Map<String, FileParam> fileParams;
  private final Map<String, Object> bodyParams;

  public MultipartParams(Map<String, FileParam> fileParams, Map<String, Object> bodyParams) {
    this.fileParams = fileParams;
    this.bodyParams = bodyParams;
  }

  public Map<String, FileParam> fileParams() {
    return fileParams;
  }

  public Map<String, Object> bodyParams() {
    return bodyParams;
  }



}
