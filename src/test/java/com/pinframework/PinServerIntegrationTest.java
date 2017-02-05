package com.pinframework;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import com.pinframework.upload.PinFileParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@Test
public class PinServerIntegrationTest {

  private static final int PORT = 7777;
  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();

  private PinServer pinServer;

  private Path externalFolder;

  private Path externalTextFile;

  private Path externalHtmlFile;

  /**
   * Initialize the server to test.
   * 
   * @throws IOException if can not create temp files
   */
  @BeforeClass
  public void setUp() throws IOException {
    externalFolder = Files.createTempDirectory("external-temp-folder");
    externalTextFile = Files.createTempFile(externalFolder, "external-txt-file", ".txt");
    Files.write(externalTextFile, "external-txt-file-content".getBytes(StandardCharsets.UTF_8));
    externalHtmlFile = Files.createTempFile(externalFolder, "external-html-file", ".html");
    Files.write(externalHtmlFile,
        "<html><body>external-html-file-content</body></html>".getBytes(StandardCharsets.UTF_8));

    //@formatter:off
    pinServer = new PinServerBuilder()
        .port(PORT)
        .appContext("integration-test")
        .externalFolder(externalFolder.toString())
        .uploadSupportEnabled(true)
        .webjarsPreferMinified(true)
        .build();
    //@formatter:on
    pinServer.onGet("text-no-params", pinExchange -> PinResponses.okText("sample-text"));

    pinServer.onGet("accept-depending", "text/plain", pinExchange -> {
      return PinResponses.okText("accept-depending-text");
    });
    pinServer.onGet("accept-depending", "application/json", pinExchange -> {
      Map<String, String> map = new HashMap<>();
      map.put("key", "accept-depending-json");
      return PinResponses.okJson(map);
    });

    pinServer.onGet("text-path-params/:first-key/separator/:second-key", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("path-params\n");
      sb.append("first-key:").append(pinExchange.pathParams().get("first-key")).append('\n');
      sb.append("second-key:").append(pinExchange.pathParams().get("second-key")).append('\n');
      return PinResponses.okText(sb.toString());
    });

    pinServer.onGet("text-query-params", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("query-params\n");
      sb.append("first-key[0]:").append(pinExchange.queryParams().get("first-key").get(0))
          .append('\n');
      sb.append("second-key[0]:").append(pinExchange.queryParams().get("second-key").get(0))
          .append('\n');
      sb.append("second-key[1]:").append(pinExchange.queryParams().get("second-key").get(1))
          .append('\n');
      return PinResponses.okText(sb.toString());
    });
    pinServer.onPost("text-body-params", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("body-params\n");
      Map<String, Object> postParams = pinExchange.bodyParams();
      sb.append("first-key[0]:").append(((List<?>) postParams.get("first-key")).get(0))
          .append('\n');
      sb.append("second-key[0]:").append(((List<?>) postParams.get("second-key")).get(0))
          .append('\n');
      sb.append("second-key[1]:").append(((List<?>) postParams.get("second-key")).get(1))
          .append('\n');
      return PinResponses.okText(sb.toString());
    });
    pinServer.onPost("text-file-params", pinExchange -> {
      StringBuilder sb = new StringBuilder();
      sb.append("file-params\n");
      Map<String, PinFileParam> fileParams = pinExchange.fileParams();
      PinFileParam fileParam = fileParams.get("file-param-key");
      sb.append("file-name:").append(fileParam.getName()).append('\n');
      sb.append("file-content:").append(new String(fileParam.content(), StandardCharsets.UTF_8))
          .append('\n');
      sb.append("body-params\n");
      Map<String, Object> postParams = pinExchange.bodyParams();
      sb.append("first-key:").append(postParams.get("first-key")).append('\n');
      sb.append("second-ke:").append(postParams.get("second-key")).append('\n');
      return PinResponses.okText(sb.toString());
    });

    pinServer.onGet("file-to-download", pinExchange -> {
      return PinResponses.okDownload("file-content", "file-name.txt");
    });

    pinServer.start();
  }

  /**
   * Shuts down server, clear temp files.
   * 
   */
  @AfterClass
  public void tearDown() throws IOException, InterruptedException {
    pinServer.stop(1);
    Thread.sleep(2000);
    Files.delete(externalHtmlFile);
    Files.delete(externalTextFile);
    Files.delete(externalFolder);
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
  public void acceptDependingText() throws IOException {
    Request request = new Request.Builder().header("Accept", "text/plain")
        .url("http://localhost:" + PORT + "/integration-test/accept-depending").build();

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
        .url("http://localhost:" + PORT + "/integration-test/accept-depending").build();

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
        .url("http://localhost:" + PORT + "/integration-test/text-no-params-non-existent").build();

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
        new Request.Builder().url("http://localhost:" + PORT + "/integration-test/text-body-params")
            .post(formBody).build();

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
        new Request.Builder().url("http://localhost:" + PORT + "/integration-test/text-body-params")
            .post(jsonBody).build();

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
        new Request.Builder().url("http://localhost:" + PORT + "/integration-test/text-file-params")
            .post(formBody).build();

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
  public void externalTextFileFound() throws IOException {
    Request request = new Request.Builder()
        .url(
            "http://localhost:" + PORT + "/integration-test/" + externalTextFile.toFile().getName())
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "external-txt-file-content");
  }

  @Test
  public void externalPostFile() throws IOException {
    Request request = new Request.Builder()
        .url(
            "http://localhost:" + PORT + "/integration-test/" + externalTextFile.toFile().getName())
        .post(RequestBody.create(MediaType.parse("text/plain"), "content")).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    String bodyAsString = response.body().string();
    assertTrue(bodyAsString.startsWith("Can not find /integration-test/external-txt-file"));
  }

  @Test
  public void externalHtmlFileFound() throws IOException {
    Request request = new Request.Builder()
        .url(
            "http://localhost:" + PORT + "/integration-test/" + externalHtmlFile.toFile().getName())
        .build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>external-html-file-content</body></html>");
  }

  @Test
  public void internalTextFileFound() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/internal-txt-file.txt").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "internal-txt-file-content");
  }

  @Test
  public void internalHtmlFileFound() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/internal-html-file.html").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>internal-html-file-content</body></html>");
  }

  @Test
  public void tryToTraversalTree() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/../../../").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<h1>404 Not Found</h1>No context found for request");
  }

  @Test
  public void downloadFile() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + PORT + "/integration-test/file-to-download").build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "application");
    assertEquals(response.body().contentType().subtype(), "force-download");
    assertEquals(response.body().string(), "file-content");
    assertTrue(response.header("Content-Disposition").contains("file-name"));
  }

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
        "Error trying to access '/integration-test/webjars/normalize.css/3.0.2/normalize.css', wrong method 'POST'");
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


