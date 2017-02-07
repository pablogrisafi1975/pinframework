package com.pinframework;


import static com.pinframework.IntegrationSuiteListener.BASE_URL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(IntegrationSuiteListener.class)
@Test(groups = "integration", suiteName = "integration")
public class PinServerSetupIntegrationTest {


  private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.MINUTES)
      .connectTimeout(5, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();


  @Test
  public void externalTextFileFound(ITestContext context) throws IOException {
    Path externalTextFile = (Path) context.getSuite().getAttribute("externalTextFile");
    Request request =
        new Request.Builder().url(BASE_URL + externalTextFile.toFile().getName()).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    assertEquals(response.body().string(), "external-txt-file-content");
  }

  @Test
  public void externalPostFile(ITestContext context) throws IOException {
    Path externalTextFile = (Path) context.getSuite().getAttribute("externalTextFile");
    Request request = new Request.Builder().url(BASE_URL + externalTextFile.toFile().getName())
        .post(RequestBody.create(MediaType.parse("text/plain"), "content")).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 404);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "plain");
    String bodyAsString = response.body().string();
    assertTrue(bodyAsString.startsWith("Can not find /integration-test/external-txt-file"));
  }

  @Test
  public void externalHtmlFileFound(ITestContext context) throws IOException {
    Path externalHtmlFile = (Path) context.getSuite().getAttribute("externalHtmlFile");
    Request request =
        new Request.Builder().url(BASE_URL + externalHtmlFile.toFile().getName()).build();

    Response response = client.newCall(request).execute();

    assertEquals(response.code(), 200);
    assertEquals(response.body().contentType().type(), "text");
    assertEquals(response.body().contentType().subtype(), "html");
    assertEquals(response.body().string(), "<html><body>external-html-file-content</body></html>");
  }


}


