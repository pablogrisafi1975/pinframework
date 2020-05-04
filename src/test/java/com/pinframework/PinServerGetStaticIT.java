package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerGetStaticIT {

    private PinServer pinServer;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeAll
    public void setup() throws IOException {
        Path tempDirectory = Files.createTempDirectory("prefix");
        Path indexFilePath = Path.of(tempDirectory.toString(), "index.html");
        Files.createFile(indexFilePath);
        Files.write(indexFilePath, "<html><h1>Index</h1></html>".getBytes(StandardCharsets.UTF_8));

        pinServer = new PinServerBuilder().externalFolder(tempDirectory.toAbsolutePath().toString()).webjarsSupportEnabled(true).build();

        pinServer.onGet("constant-text", PinResponse.ok("this is the constant text"), PinRenderType.TEXT);
        pinServer.start();
    }

    @Test
    public void getConstantText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/constant-text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("this is the constant text", response.body().string());
        }
    }

    @Test
    public void getFromExternalFolder() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/index.html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("<html><h1>Index</h1></html>", response.body().string());
        }
    }

    @Test
    public void getFromExternalFolderByDefault() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("<html><h1>Index</h1></html>", response.body().string());
        }
    }

    @Test
    public void getFromExternalFolderTraversalAttack() throws IOException {
        Request request = new Request.Builder()
                //%2e%2e%2f = ../ urlEncoded
                .url("http://localhost:9999/yy/%2e%2e%2f%2e%2e%2findex2.html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("Error on filename 'yy/../../index2.html', directory traversal attack", response.body().string());
        }
    }

    @Test
    public void getFromExternalFolderNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/not-found.html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("File 'not-found.html' not found in 'static/'", response.body().string());
        }
    }

    @Test
    public void deleteFromExternalFolder() throws IOException {
        Request request = new Request.Builder()
                .delete()
                .url("http://localhost:9999/index.html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("Error trying to access '/index.html', wrong method 'DELETE'", response.body().string());
        }
    }

    @Test
    public void getFromWebJar() throws IOException {
        //envjs 1.2 is declared in pom.xml as test dependency
        Request request = new Request.Builder()
                .url("http://localhost:9999/webjars/envjs/1.2/env.rhino.js")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals("application/javascript", response.header(PinContentType.CONTENT_TYPE));
            assertTrue(response.body().string().startsWith("/*\n"
                    + " * Envjs core-env.1.2.13\n"
                    + " * Pure JavaScript Browser Environment\n"
                    + " * By John Resig <http://ejohn.org/> and the Envjs Team\n"
                    + " * Copyright 2008-2010 John Resig, under the MIT License\n"
                    + " */"));
        }
    }

    @Test
    public void getFromWebJarNotFound() throws IOException {
        //envjs 1.2 is declared in pom.xml as test dependency
        Request request = new Request.Builder()
                .url("http://localhost:9999/webjars/envjs/1.2/env.rhino.gif")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.code());
            assertEquals("image/gif", response.header(PinContentType.CONTENT_TYPE));
            assertEquals("File '/envjs/1.2/env.rhino.gif' not found in 'META-INF/resources/webjars'", response.body().string());
        }
    }

    @Test
    public void deleteFromWebJar() throws IOException {
        Request request = new Request.Builder()
                .delete()
                .url("http://localhost:9999/webjars/envjs/1.2/env.rhino.gif")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.code());
            assertEquals("image/gif", response.header(PinContentType.CONTENT_TYPE));
            assertEquals("Error trying to access '/webjars/envjs/1.2/env.rhino.gif', wrong method 'DELETE'", response.body().string());
        }
    }

    @Test
    public void getFromResourceFolder() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/style.css")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals("text/css", response.header(PinContentType.CONTENT_TYPE));
            assertEquals("p {\n"
                    + "  color: red;\n"
                    + "  text-align: center;\n"
                    + "}", response.body().string());
        }
    }

    @Test
    public void deleteFromEResourceFolder() throws IOException {
        Request request = new Request.Builder()
                .delete()
                .url("http://localhost:9999/style.css")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_METHOD, response.code());
            assertEquals("text/css", response.header(PinContentType.CONTENT_TYPE));
            assertEquals("Error trying to access '/style.css', wrong method 'DELETE'", response.body().string());
        }
    }


    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }
}