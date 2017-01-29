package com.pinframework.requestmatcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class PinExternalFileRequestMatcherTest {

  private PinExternalFileRequestMatcher pinExternalFileRequestMatcher;
  private File externalFolder;

  /**
   * Create a valid external folder.
   * @throws IOException if can't create temporary folder
   */
  @BeforeClass
  public void setUp() throws IOException {
    externalFolder = Files.createTempDirectory("prefix").toFile();
    pinExternalFileRequestMatcher =
        new PinExternalFileRequestMatcher("/app-context/", externalFolder);
  }

  @AfterClass
  public void tearDown() throws IOException {
    Files.delete(externalFolder.toPath());
  }


  @Test
  public void matches() throws Exception {
    File file = new File(externalFolder, "file.html");
    Files.createFile(file.toPath());
    try {
      assertTrue(pinExternalFileRequestMatcher.matches("GET", "/app-context/file.html", null));
    } finally {
      Files.delete(file.toPath());
    }
  }

  @Test
  public void matchesDefaultFile() throws Exception {
    File file = new File(externalFolder, "index.html");
    Files.createFile(file.toPath());
    try {
      assertTrue(pinExternalFileRequestMatcher.matches("GET", "/app-context/", null));
    } finally {
      Files.delete(file.toPath());
    }
  }

  @Test
  public void matchesNotGet() throws Exception {
    File file = new File(externalFolder, "file.html");
    Files.createFile(file.toPath());
    try {
      assertFalse(pinExternalFileRequestMatcher.matches("POST", "/app-context/file.html", null));
    } finally {
      Files.delete(file.toPath());
    }
  }

  @Test
  public void matchesNoExternalFolderConfigured() throws Exception {
    PinExternalFileRequestMatcher noFolder = new PinExternalFileRequestMatcher("app-context", null);
    File file = new File(externalFolder, "file.html");
    Files.createFile(file.toPath());
    try {
      assertFalse(noFolder.matches("GET", "/app-context/file.html", null));
    } finally {
      Files.delete(file.toPath());
    }
  }

  @Test
  public void matchesTraversalAttempt() throws Exception {
    File file = new File(externalFolder.getParentFile(), "file.html");
    Files.createFile(file.toPath());
    try {
      assertFalse(pinExternalFileRequestMatcher.matches("GET", "/app-context/../file.html", null));
    } finally {
      Files.delete(file.toPath());
    }
  }

  @Test
  public void extractPathParams() throws Exception {
    Map<String, String> pathParams =
        pinExternalFileRequestMatcher.extractPathParams("/app-context/file.html");
    assertEquals(pathParams.get(PinExternalFileRequestMatcher.FILE_NAME), "file.html");
    File externalFile = new File(externalFolder, "file.html");
    assertEquals(pathParams.get(PinExternalFileRequestMatcher.EXTERNAL_FILE_NAME),
        externalFile.getAbsolutePath());
  }

}
