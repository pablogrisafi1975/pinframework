package com.pinframework;

import com.pinframework.upload.PinFileParam;
import com.sun.net.httpserver.HttpExchange;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PinExchange {

  private final HttpExchange httpExchange;
  private Map<String, List<String>> queryParams;
  private Map<String, Object> bodyParams;
  private Map<String, String> pathParams;
  private Map<String, PinFileParam> fileParams;

  /**
   * Creates a PinExchange that wraps an HttpExchange.
   * 
   * @param httpExchange the HttpExchange to wrap
   * @param pathParams the path encoded paramaters
   * @param queryParams the query string paramters
   * @param bodyParams the body parameter, if they are not files.
   * @param fileParams the posted files
   */
  public PinExchange(HttpExchange httpExchange, Map<String, String> pathParams,
      Map<String, List<String>> queryParams, Map<String, Object> bodyParams,
      Map<String, PinFileParam> fileParams) {
    this.httpExchange = httpExchange;
    this.pathParams = Collections.unmodifiableMap(pathParams);
    this.queryParams = Collections.unmodifiableMap(queryParams);
    this.bodyParams = Collections.unmodifiableMap(bodyParams);
    this.fileParams = Collections.unmodifiableMap(fileParams);
  }



  public HttpExchange raw() {
    return httpExchange;
  }

  public Map<String, List<String>> queryParams() {
    return queryParams;
  }

  public Map<String, String> pathParams() {
    return pathParams;
  }

  /**
   * Only actual files are returned. Extra values are present in bodyParams
   * 
   * @return The uploaded files
   */
  public Map<String, PinFileParam> fileParams() {
    return fileParams;
  }


  /**
   * The parameters in the body of the request.<br>
   * If the request has content type application/x-www-form-urlencoded, (typically when posting a
   * form via submit button or jQuery), the value will be a List&lt;String&gt;<br>
   * If the request has content type application/json, (typically when posting via Angular), the
   * value will be any JSON field (number, string, array, boolean, whatever)<br>
   * If the request has content type multipart, (typically when uploading files), the value will be
   * a unique String<br>
   * 
   * 
   * @return The parameters in the body of the request
   */
  public Map<String, Object> bodyParams() {
    return bodyParams;
  }

  /**
   * Returns the first non-null parameter with a given name. Checks path-query-body parameters in
   * this order. If there is a list of parameters with a given name, you get the first element
   * 
   * @param name The parameter name to search for
   * @return the value as String or null if not found
   */
  public String param(String name) {
    if (this.pathParams.containsKey(name)) {
      return pathParams.get(name);
    }
    if (this.queryParams.containsKey(name)) {
      return queryParams.get(name).get(0);
    }
    if (this.bodyParams.containsKey(name)) {
      Object value = bodyParams.get(name);
      if (value instanceof Collection) {
        Collection<?> col = (Collection<?>) value;
        if (col.size() > 0) {
          return (String) col.iterator().next();
        }
        return null;
      }
      if (value instanceof Object[]) {
        Object[] array = (Object[]) value;
        if (array.length > 0) {
          Object retVal = array[0];
          if (retVal == null) {
            return null;
          }
          return retVal.toString();
        }
        return null;
      }
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }


  public Map<String, List<String>> requestHeaders() {
    // only because headers class has restrictions
    return httpExchange.getRequestHeaders();
  }

  public String firstRequestHeader(String name) {
    // only because headers class has restrictions
    return httpExchange.getRequestHeaders().getFirst(name);
  }


  public Map<String, List<String>> responseHeaders() {
    // only because headers class has restrictions
    return httpExchange.getResponseHeaders();
  }

  public String firstResponseHeaders(String name) {
    // only because headers class has restrictions
    return httpExchange.getResponseHeaders().getFirst(name);
  }

  public Object getAttribute(String key) {
    return httpExchange.getAttribute(key);
  }



}
