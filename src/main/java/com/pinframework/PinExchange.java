package com.pinframework;

import com.pinframework.upload.FileParam;
import com.sun.net.httpserver.HttpExchange;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PinExchange {

  // TODO: scar los get
  // TODO: renombrrar postParam a bodyParams
  // TODO: metodo param que da el primero como string

  private final HttpExchange httpExchange;
  private Map<String, List<String>> queryParams;
  private Map<String, Object> postParams;
  private Map<String, String> pathParams;
  private Map<String, FileParam> fileParams;

  /**
   * Creates a PinExchange that wraps an HttpExchange.
   * 
   * @param httpExchange the HttpExchange to wrap
   * @param pathParams the path encoded paramaters
   * @param queryParams the query string paramters
   * @param postParams the post parameter, if they are not files
   * @param fileParams the posted files
   */
  public PinExchange(HttpExchange httpExchange, Map<String, String> pathParams,
      Map<String, List<String>> queryParams, Map<String, Object> postParams,
      Map<String, FileParam> fileParams) {
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
   * @return The uploaded files
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

  public Object getAttribute(String key) {
    return httpExchange.getAttribute(key);
  }



}
