package com.pinframework;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.pinframework.exception.PinInitializationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.testng.annotations.Test;

import sun.net.httpserver.HttpServerImpl;
import sun.net.httpserver.HttpsServerImpl;

@Test
public class PinServerBuilderTest {

  private final PinServerBuilder pinServerBuilder = new PinServerBuilder();

  @Test(expectedExceptions = PinInitializationException.class)
  public void port_tooLow() throws Exception {
    pinServerBuilder.port(-1);
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void port_tooHigh() throws Exception {
    pinServerBuilder.port(888888);
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_appContextStartWithSlash() throws Exception {
    new PinServerBuilder().appContext("/app-context").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_appContextEndsWithSlash() throws Exception {
    new PinServerBuilder().appContext("app-context/").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void maxBacklog_negative() throws Exception {
    pinServerBuilder.maxBacklog(-1);
  }

  @Test
  public void build_defaultValues() throws Exception {
    PinServer pinServer = new PinServerBuilder().build();
    assertEquals(pinServer.port, PinServerBuilder.DEFAULT_PORT);
    assertEquals(pinServer.restrictedCharset, PinServerBuilder.DEFAULT_RESTRICTED_CHARSET);
    assertEquals(pinServer.appContext, "/");
    assertTrue(pinServer.httpServer instanceof HttpServerImpl);
    Executor executor = pinServer.httpServer.getExecutor();
    assertTrue(executor instanceof ThreadPoolExecutor);
    assertEquals(((ThreadPoolExecutor) executor).getMaximumPoolSize(), 10);
    assertEquals(((ThreadPoolExecutor) executor).getCorePoolSize(), 10);
  }

  @Test
  public void build_otherValues() throws Exception {
    //@formatter:off
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    PinServer pinServer = new PinServerBuilder()
        .port(7864)
        .restrictedCharset(true)
        .appContext("app-context")
        .httpsSupportEnabled(true)
        .executor(singleThreadExecutor)
        .build();
    //@formatter:on
    assertEquals(pinServer.port, 7864);
    assertEquals(pinServer.restrictedCharset, true);
    assertEquals(pinServer.appContext, "/app-context/");
    assertTrue(pinServer.httpServer instanceof HttpsServerImpl);
    assertEquals(pinServer.httpServer.getExecutor(), singleThreadExecutor);
  }

  public void build_appContextNull() throws Exception {
    PinServer pinServer = new PinServerBuilder().appContext(null).port(5555).build();
    assertEquals(pinServer.appContext, "/");
  }

  public void build_appContextEmpty() throws Exception {
    PinServer pinServer = new PinServerBuilder().appContext("  ").port(5556).build();
    assertEquals(pinServer.appContext, "/");
  }

  public void build_appContextSomething() throws Exception {
    PinServer pinServer = new PinServerBuilder().appContext("something").port(5557).build();
    assertEquals(pinServer.appContext, "/something/");
  }

  @Test
  public void build_appContextRestrictedValid() throws Exception {
    PinServer pinServer =
        new PinServerBuilder().appContext("this-is.valid1234567890").port(5558).build();
    assertEquals(pinServer.appContext, "/this-is.valid1234567890/");
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_appContextRestrictedUppercase() throws Exception {
    new PinServerBuilder().restrictedCharset(true).appContext("A").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_appContextRestrictedSymbol() throws Exception {
    new PinServerBuilder().restrictedCharset(true).appContext("a+b").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_appContextRestrictedNonAscii() throws Exception {
    new PinServerBuilder().restrictedCharset(true).appContext("é").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_noWebjarSupportButPreferMinified() throws Exception {
    new PinServerBuilder().webjarsSupportEnabled(false).webjarsPreferMinified(true).build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_externalFolderNonCanonical() throws Exception {
    new PinServerBuilder().externalFolder(">1").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_externalFolderNonExisting() throws Exception {
    new PinServerBuilder().externalFolder("Im-sure-ther-is-no-folder-named-like-this").build();
  }

  @Test(expectedExceptions = PinInitializationException.class)
  public void build_externalFolderNotAFolder() throws Exception {
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("prefix", "suffix");
      new PinServerBuilder().externalFolder(tempFile.toFile().getAbsolutePath()).build();
    } finally {
      Files.delete(tempFile);
    }
  }



}
