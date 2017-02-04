package com.pinframework;

import static org.testng.Assert.assertEquals;

import com.pinframework.upload.FileParam;
import com.sun.net.httpserver.HttpExchange;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test
public class PinExchangeTest {

  private HttpExchange httpExchange;

  private PinExchange pinExchange;

  @BeforeTest
  public void setUp() throws Exception {
    httpExchange = null;
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("pathKey0", "pathValue0");
    pathParams.put("pathKey1", "pathValue1");

    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("queryKey0", Arrays.asList("queryValue00", "queryValue01"));
    queryParams.put("queryKey1", Arrays.asList("queryValue10", "queryValue11"));

    Map<String, Object> bodyParams = new HashMap<>();
    bodyParams.put("bodyKey0", "bodyValue0");
    bodyParams.put("bodyKey1", 1);
    bodyParams.put("bodyKey2", Arrays.asList("bodyValue20", "bodyValue21"));
    bodyParams.put("bodyKey3", new String[] {"bodyValue30", "bodyValue31"});
    bodyParams.put("bodyKey4", new Integer[] {40, 41});
    bodyParams.put("bodyKey5", new Integer[] {});
    bodyParams.put("bodyKey6", new Integer[] {null});
    bodyParams.put("bodyKey7", Arrays.asList());
    bodyParams.put("bodyKey8", null);

    Map<String, FileParam> fileParams = new HashMap<>();
    pinExchange = new PinExchange(httpExchange, pathParams, queryParams, bodyParams, fileParams);
  }

  @Test
  public void raw() throws Exception {
    //
  }

  @Test
  public void queryParams() throws Exception {
    assertEquals(pinExchange.queryParams().get("queryKey0").get(0), "queryValue00");
    assertEquals(pinExchange.queryParams().get("queryKey0").get(1), "queryValue01");
    assertEquals(pinExchange.queryParams().get("queryKey1").get(0), "queryValue10");
    assertEquals(pinExchange.queryParams().get("queryKey1").get(1), "queryValue11");
  }

  @Test
  public void pathParams() throws Exception {
    assertEquals(pinExchange.pathParams().get("pathKey0"), "pathValue0");
    assertEquals(pinExchange.pathParams().get("pathKey1"), "pathValue1");
  }

  @Test
  public void fileParams() throws Exception {
    //
  }

  @Test
  public void bodyParams() throws Exception {
    assertEquals(pinExchange.bodyParams().get("bodyKey0"), "bodyValue0");
    assertEquals(pinExchange.bodyParams().get("bodyKey1"), Integer.valueOf(1));
    assertEquals(pinExchange.bodyParams().get("bodyKey2"),
        Arrays.asList("bodyValue20", "bodyValue21"));
    assertEquals(pinExchange.bodyParams().get("bodyKey3"),
        new String[] {"bodyValue30", "bodyValue31"});
    assertEquals(pinExchange.bodyParams().get("bodyKey4"), new Integer[] {40, 41});
    assertEquals(pinExchange.bodyParams().get("bodyKey5"), new Integer[] {});
    assertEquals(pinExchange.bodyParams().get("bodyKey6"), new Integer[] {null});
  }

  @Test
  public void param() throws Exception {
    assertEquals(pinExchange.param("queryKey0"), "queryValue00");
    assertEquals(pinExchange.param("queryKey1"), "queryValue10");

    assertEquals(pinExchange.param("pathKey0"), "pathValue0");
    assertEquals(pinExchange.param("pathKey1"), "pathValue1");

    assertEquals(pinExchange.param("bodyKey0"), "bodyValue0");
    assertEquals(pinExchange.param("bodyKey1"), "1");
    assertEquals(pinExchange.param("bodyKey2"), "bodyValue20");
    assertEquals(pinExchange.param("bodyKey3"), "bodyValue30");
    assertEquals(pinExchange.param("bodyKey4"), "40");
    assertEquals(pinExchange.param("bodyKey5"), null);
    assertEquals(pinExchange.param("bodyKey6"), null);
    assertEquals(pinExchange.param("bodyKey7"), null);
    assertEquals(pinExchange.param("bodyKey8"), null);
    assertEquals(pinExchange.param("bodyTherIsNoKey"), null);

  }

  @Test
  public void requestHeaders() throws Exception {
    //
  }

  @Test
  public void responseHeaders() throws Exception {
    //
  }

  @Test
  public void getAttribute() throws Exception {
    //
  }

}
