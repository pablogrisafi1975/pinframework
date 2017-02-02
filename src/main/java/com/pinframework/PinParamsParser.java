package com.pinframework;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinParamsParser {

  private static final Logger LOG = LoggerFactory.getLogger(PinParamsParser.class);

  private Gson gsonParser;

  public PinParamsParser(Gson gsonParser) {
    this.gsonParser = gsonParser;
  }

  /**
   * Parses the query parameters. Based on
   * http://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
   * 
   * @param rawQuery The query string, not decoded
   * @return The parameters parsed
   */
  public Map<String, List<String>> queryParams(String rawQuery) {
    if (rawQuery == null || rawQuery.trim().length() == 0) {
      return Collections.emptyMap();
    }
    final Map<String, List<String>> queryPairs = new LinkedHashMap<String, List<String>>();
    final String[] pairs = rawQuery.split("&");
    for (String pair : pairs) {
      final int idx = pair.indexOf("=");
      final String key = idx > 0 ? PinUtils.urlDecode(pair.substring(0, idx)) : pair;
      if (!queryPairs.containsKey(key)) {
        queryPairs.put(key, new LinkedList<>());
      }
      final String value =
          idx > 0 && pair.length() > idx + 1 ? PinUtils.urlDecode(pair.substring(idx + 1)) : null;
      queryPairs.get(key).add(value);
    }
    return queryPairs;
  }



  /**
   * Check if a contentType is multipart.
   * 
   * @param fullContentType The full content type header or null
   * @return true if starts with "multipart"
   */
  public boolean isMultipart(String fullContentType) {
    if (fullContentType == null) {
      return false;
    }
    return fullContentType.startsWith(PinContentType.MULTIPART);
  }

  /**
   * Parses the posted parameters according to the content type.
   * 
   * @param fullContentType The complete content type
   * @param requestBody The body to parse
   * @return a map of parameters. Will be empty if content type is null of invalid
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> postParams(String fullContentType, InputStream requestBody) {
    if (fullContentType == null) {
      String body = PinUtils.asString(requestBody);
      if (body != null && body.trim().length() > 0) {
        LOG.error("No content type, parameters can not be parsed, body was {}", body);
      }
      return Collections.emptyMap();
    } else if (fullContentType.startsWith(PinContentType.APPLICATION_JSON)) {
      // that's angular encoding by default
      return gsonParser.fromJson(new InputStreamReader(requestBody, StandardCharsets.UTF_8),
          HashMap.class);
    } else if (fullContentType.startsWith(PinContentType.APPLICATION_FORM_URLENCODED)) {
      String postData = PinUtils.urlDecode(PinUtils.asString(requestBody));
      Map<String, List<String>> splitQuery = queryParams(postData);
      return splitQuery.entrySet().stream()
          .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
    LOG.error("Unknown content type, parameters can not be parsed, body was {}",
        PinUtils.asString(requestBody));
    return Collections.emptyMap();
  }

}
