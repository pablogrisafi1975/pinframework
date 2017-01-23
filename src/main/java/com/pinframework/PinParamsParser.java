package com.pinframework;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
   * Parses the query parameters.
   * @param decodedQuery The query string, already decoded
   * @return The parameters parsed
   */
  public Map<String, List<String>> queryParams(String decodedQuery) {
    if (decodedQuery == null || decodedQuery.trim().length() == 0) {
      return Collections.emptyMap();
    }
    return Arrays.stream(decodedQuery.split("&")).map(this::splitQueryParameter)
        .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  private SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
    final int idx = it.indexOf('=');
    final String key = idx > 0 ? it.substring(0, idx) : it;
    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
    return new SimpleImmutableEntry<>(key, value);
  }

  /**
   * Check if a contentType is multipart.
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
   * @param fullContentType The complete content type
   * @param requestBody The body to parse
   * @return a map of parameters. Will be empty if content type is null of invalid
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> postParams(String fullContentType, InputStream requestBody) {
    if (fullContentType == null) {
      LOG.error("No content type, parameters can not be parsed");
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
    LOG.error("Unknown content type, parameters can not be parsed");
    return Collections.emptyMap();
  }

}
