package com.pinframework;

import static com.pinframework.IntegrationSuiteListener.APP_CONTEXT;
import static com.pinframework.IntegrationSuiteListener.BASE_URL;
import static com.pinframework.IntegrationSuiteListener.PORT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(IntegrationSuiteListener.class)
@Test(groups = "integration", suiteName = "integration")
public class PinServerServicesIntegrationTest {

  private static final String STRIPPED_APP_CONTEXT = APP_CONTEXT.replaceAll("/", "");
  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();


  @Test
  public void textNoParamsFound() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "text-no-params").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "sample-text");
  }

  @Test
  public void textNoParamsNotFound() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "text-no-params-non-existent").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().string(),
        "Can not find /integration-test/text-no-params-non-existent");
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
  }

  @Test
  public void acceptDependingText() throws IOException {
    Request request = new Request.Builder().header("Accept", "text/plain")
        .url(BASE_URL + "accept-depending").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "accept-depending-text");
  }

  @Test
  public void acceptDependingJson() throws IOException {
    Request request = new Request.Builder().header("Accept", "application/json")
        .url(BASE_URL + "accept-depending").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "application");
    assertEquals(response.body().contentType().subtype(), "json");
    assertEquals(response.body().string(), "{\"key\":\"accept-depending-json\"}");
  }

  @Test
  public void textNoParamsNotFoundAcceptJson() throws IOException {
    Request request = new Request.Builder().header("Accept", "application/json")
        .url(BASE_URL + "text-no-params-non-existent").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().string(),
        "{\"requestUri\":\"/integration-test/text-no-params-non-existent\"}");
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "application");
    assertEquals(response.body().contentType().subtype(), "json");
  }

  @Test
  public void textPathParamsFound() throws IOException {
    Request request = new Request.Builder()
        .url(BASE_URL + "text-path-params/first-value/separator/second-value").build();

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
    Request request =
        new Request.Builder().url(BASE_URL + "text-path-params/áéíóú/separator/ññññ").build();

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
        .addPathSegment(STRIPPED_APP_CONTEXT).addPathSegment("text-query-params")
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
        .addPathSegment(STRIPPED_APP_CONTEXT).addPathSegment("text-query-params")
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
        .addPathSegment(STRIPPED_APP_CONTEXT).addPathSegment("text-query-params")
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

  @Test
  public void textBodyParamsAsFormFound() throws IOException {
    //@formatter:off
    RequestBody formBody = new FormBody.Builder()
        .add("first-key", "first-value")
        .add("second-key", "second-value-0")
        .add("second-key", "second-value-1")
        .build();
    //@formatter:on

    Request request =
        new Request.Builder().url(BASE_URL + "text-body-params").post(formBody).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "body-params\nfirst-key[0]:first-value\n"
        + "second-key[0]:second-value-0\nsecond-key[1]:second-value-1\n");
  }

  @Test
  public void textBodyParamsAsJsonFound() throws IOException {
    MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    Map<String, Object> map = new HashMap<>();
    map.put("first-key", Collections.singletonList("first-value"));
    map.put("second-key", Arrays.asList("second-value-0", "second-value-1"));

    String json = new Gson().toJson(map);
    RequestBody jsonBody = RequestBody.create(jsonMediaType, json);
    Request request =
        new Request.Builder().url(BASE_URL + "text-body-params").post(jsonBody).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "body-params\nfirst-key[0]:first-value\n"
        + "second-key[0]:second-value-0\nsecond-key[1]:second-value-1\n");
  }

  @Test
  public void textFileParamsFound() throws IOException, InterruptedException {
    RequestBody fileContent = RequestBody.create(MediaType.parse("text/plain"),
        "this is the content".getBytes(StandardCharsets.UTF_8));
    //@formatter:off
    RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file-param-key", "fileName.txt", fileContent)
        .addFormDataPart("first-key", "first-key")
        .addFormDataPart("second-key", "second-value").build();
    //@formatter:on

    Request request =
        new Request.Builder().url(BASE_URL + "text-file-params").post(formBody).build();

    Response response = client.newCall(request).execute();
    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().charset(), StandardCharsets.UTF_8);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(),
        "file-params\n" + "file-name:fileName.txt\n" + "file-content:this is the content\n"
            + "body-params\n" + "first-key:first-key\n" + "second-ke:second-value\n");

  }


  @Test
  public void downloadFile() throws IOException {
    Request request = new Request.Builder().url(BASE_URL + "file-to-download").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "application");
    assertEquals(response.body().contentType().subtype(), "force-download");
    assertEquals(response.body().string(), "file-content");
    assertTrue(response.header("Content-Disposition").contains("file-name"));
  }


}


