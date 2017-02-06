package com.pinframework;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.pinframework.upload.PinFileParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


@Test(groups = "integration")
public class PinServerSetupIntegrationTest {

  private static final int PORT = 7777;

  private PinServer pinServer;

  private Path externalFolder;

  private Path externalTextFile;

  private Path externalHtmlFile;

  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();


  /**
   * Initialize the server to test.
   * 
   * @throws IOException if can not create temp files
   */
  @BeforeSuite
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
  @AfterSuite
  public void tearDown() throws IOException, InterruptedException {
    pinServer.stop(1);
    Thread.sleep(2000);
    Files.delete(externalHtmlFile);
    Files.delete(externalTextFile);
    Files.delete(externalFolder);
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


}


