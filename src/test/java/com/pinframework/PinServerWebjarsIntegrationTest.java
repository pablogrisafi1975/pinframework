package com.pinframework;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.testng.annotations.Test;


@Test(groups = "integration")
public class PinServerWebjarsIntegrationTest {

  private static final int PORT = 7777;

  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();

  @Test
  public void webjarsFound() throws IOException {
    Request request = new Request.Builder().url(
        "http://localhost:" + PORT + "/integration-test/webjars/normalize.css/3.0.2/normalize.css")
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "css");
    assertTrue(response.body().string()
        .startsWith("/*! normalize.css v3.0.2 | MIT License | git.io/normalize */"));
  }

  @Test
  public void webjarsPost() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT
            + "/integration-test/webjars/normalize.css/3.0.2/normalize.css")
        .post(RequestBody.create(MediaType.parse("text/plain"),
            "new content".getBytes(StandardCharsets.UTF_8)))
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 405);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "Error trying to access '/integration-test/webjars/normalize.css/3.0.2/normalize.css'"
            + ", wrong method 'POST'");
  }

  @Test
  public void webjarsMinifiedFound() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/webjars/animate.css/3.5.2/animate.css")
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "css");
    assertTrue(response.body().string()
        .contains(".animated{-webkit-animation-duration:1s;animation-duration:1s;"));
  }

  @Test
  public void webjarsNotFound() throws IOException {
    Request request = new Request.Builder().url("http://localhost:" + PORT
        + "/integration-test/webjars/normalize.css/3.0.2/normalize-wrong.css").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "File '/normalize.css/3.0.2/normalize-wrong.css' not found in webjars");
  }


}


