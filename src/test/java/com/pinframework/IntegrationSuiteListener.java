package com.pinframework;

import com.pinframework.upload.PinFileParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class IntegrationSuiteListener implements ISuiteListener {

  private static final Logger LOG = LoggerFactory.getLogger(IntegrationSuiteListener.class);


  public static final int PORT = 7777;
  public static final String APP_CONTEXT = "/integration-test/";

  /**
   * Includes the last /, something like http://localhost:7777/integration-test/ .
   */
  public static final String BASE_URL = "http://localhost:" + PORT + APP_CONTEXT;

  private PinServer pinServer;

  private Path externalFolder;

  private Path externalTextFile;

  private Path externalHtmlFile;

  public static class PinTestRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PinTestRuntimeException(Throwable cause) {
      super(cause);
    }
  }

  @Override
  public void onStart(ISuite suite) {
    if (suite.getMethodsByGroups().get("integration") == null) {
      // this is a hack because testng eclipse plugin always runs listeners
      return;
    }
    LOG.info("Starting server for integration test suite");
    try {
      startServer(suite);
    } catch (Exception ex) {
      throw new PinTestRuntimeException(ex);
    }
  }

  private void startServer(ISuite suite) throws IOException {
    externalFolder = Files.createTempDirectory("external-temp-folder");
    externalTextFile = Files.createTempFile(externalFolder, "external-txt-file", ".txt");
    Files.write(externalTextFile, "external-txt-file-content".getBytes(StandardCharsets.UTF_8));
    externalHtmlFile = Files.createTempFile(externalFolder, "external-html-file", ".html");
    Files.write(externalHtmlFile,
        "<html><body>external-html-file-content</body></html>".getBytes(StandardCharsets.UTF_8));

    suite.setAttribute("externalFolder", externalFolder);
    suite.setAttribute("externalTextFile", externalTextFile);
    suite.setAttribute("externalHtmlFile", externalHtmlFile);

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

  @Override
  public void onFinish(ISuite suite) {
    if (suite.getMethodsByGroups().get("integration") == null) {
      // this is a hack because testng eclipse plugin always runs listeners
      return;
    }
    LOG.info("Stopping server for integration test suite");
    pinServer.stop(1);
    try {
      Files.delete(externalHtmlFile);
      Files.delete(externalTextFile);
      Files.delete(externalFolder);
    } catch (IOException ex) {
      throw new PinTestRuntimeException(ex);
    }
  }


}
