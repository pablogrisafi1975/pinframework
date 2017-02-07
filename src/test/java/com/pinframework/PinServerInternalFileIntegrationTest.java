package com.pinframework;


import static com.pinframework.PinServerSetupIntegrationTest.BASE_URL;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.testng.annotations.Test;


@Test(groups = "integration", suiteName = "integration")
public class PinServerInternalFileIntegrationTest {

  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();


  @Test
  public void internalTextFileFound() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "internal-txt-file.txt").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "internal-txt-file-content");
  }

  @Test
  public void internalHtmlFileFound() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "internal-html-file.html").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>internal-html-file-content</body></html>");
  }

  @Test
  public void internalIndexHtmlFileFoundWithNoFile() throws IOException {
    Request request = new Request.Builder().url(BASE_URL).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>index-html-file-content</body></html>");
  }

  @Test
  public void internalIndexHtmlFileFoundNoFileNoLastSlash() throws IOException {
    String url = BASE_URL.substring(0, BASE_URL.length() - 1);
    Request request = new Request.Builder().url(url).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>index-html-file-content</body></html>");
  }

  @Test
  public void tryToTraversalTree() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "../../../").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<h1>404 Not Found</h1>No context found for request");
  }


}


