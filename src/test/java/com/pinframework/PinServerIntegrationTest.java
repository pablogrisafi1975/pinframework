package com.pinframework;

import static org.testng.Assert.assertEquals;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class PinServerIntegrationTest {

  private static final int PORT = 7777;
  private final OkHttpClient client = new OkHttpClient();
  private PinServer pinServer;

  /**
   * Initialize the server to test.
   */
  @BeforeClass
  public void setUp() {
    // for debugging
    client.setReadTimeout(60, TimeUnit.MINUTES);
    //@formatter:off
    pinServer = new PinServerBuilder()
        .port(PORT)
        .appContext("integration-test")
        .build();
    //@formatter:on
    pinServer.onGet("text-no-params", pinExchange -> PinResponses.okText("sample-text"));
    pinServer.onGet("text-path-params/:first-key/separator/:second-key", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("path-params\n");
      sb.append("first-key:").append(pinExchange.getPathParams().get("first-key")).append('\n');
      sb.append("second-key:").append(pinExchange.getPathParams().get("second-key")).append('\n');
      return PinResponses.okText(sb.toString());
    });
    pinServer.onGet("text-query-params", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("query-params\n");
      sb.append("first-key[0]:").append(pinExchange.getQueryParams().get("first-key").get(0))
          .append('\n');
      sb.append("second-key[0]:").append(pinExchange.getQueryParams().get("second-key").get(0))
          .append('\n');
      sb.append("second-key[1]:").append(pinExchange.getQueryParams().get("second-key").get(1))
          .append('\n');
      return PinResponses.okText(sb.toString());
    });

    pinServer.start();
  }

  @AfterClass
  public void tearDown() {
    pinServer.stop(1);
  }

  @Test
  public void textNoParamsFound() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/text-no-params").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "sample-text");
  }

  @Test
  public void textNoParamsNotFound() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/text-no-params-non-existent").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().string(),
        "Can not find /integration-test/text-no-params-non-existent");
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
  }

  @Test
  public void textPathParamsFound() throws IOException {
    Request request = new Request.Builder().url("http://localhost:" + PORT
        + "/integration-test/text-path-params/first-value/separator/second-value").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "path-params\nfirst-key:first-value\nsecond-key:second-value\n");
  }

  @Test
  public void textPathParamsFoundCharset() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/text-path-params/áéíóú/separator/ññññ")
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "path-params\nfirst-key:áéíóú\nsecond-key:ññññ\n");
  }

  @Test
  public void textQueryParamsFound() throws IOException {
    HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(PORT)
        .addPathSegment("integration-test").addPathSegment("text-query-params")
        .addQueryParameter("first-key", "first-value")
        .addQueryParameter("second-key", "second-value-0")
        .addQueryParameter("second-key", "second-value-1").build();
    Request request = new Request.Builder().url(url).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "query-params\nfirst-key[0]:first-value\n"
        + "second-key[0]:second-value-0\nsecond-key[1]:second-value-1\n");
  }

  @Test
  public void textQueryParamsFoundCharsetSpaces() throws IOException {
    //@formatter:off
    HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(PORT)
        .addPathSegment("integration-test").addPathSegment("text-query-params")
        .addQueryParameter("first-key", "ááá ééé")
        .addQueryParameter("second-key", "#lalala")
        .addQueryParameter("second-key", "&a=3&")
        .build();
    //@formatter:on
    Request request = new Request.Builder().url(url).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "query-params\nfirst-key[0]:ááá ééé\n" + "second-key[0]:#lalala\nsecond-key[1]:&a=3&\n");
  }

  @Test
  public void textQueryParamsFoundCharsetEmpty() throws IOException {
    //@formatter:off
    HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(PORT)
        .addPathSegment("integration-test").addPathSegment("text-query-params")
        .addQueryParameter("first-key", null)
        .addQueryParameter("second-key", "#lalala")
        .addQueryParameter("second-key", null)
        .build();
    //@formatter:on
    Request request = new Request.Builder().url(url).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "query-params\nfirst-key[0]:null\n" + "second-key[0]:#lalala\nsecond-key[1]:null\n");
  }
}
