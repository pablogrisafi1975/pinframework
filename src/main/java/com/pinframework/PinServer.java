package com.pinframework;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinRuntimeException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class PinServer {

    private static final Logger LOG = LoggerFactory.getLogger(PinServer.class);

    /**
     * will be / or /something/
     */
    private final String appContext;
    private final HttpServer httpServer;
    private final Map<String, PinAdapter> adaptersByPath = new HashMap<>();
    private final int port;
    private final boolean restrictedCharset;
    private final Map<String, PinRender> rendersByType = new HashMap<>();
    private final PinRender defaultRender;
    private final Gson gson;

    PinServer(HttpServer httpServer, boolean restrictedCharset, String appContext, boolean webjarsSupportEnabled,
            File externalFolderCanonical, PinRender defaultRender, Gson gson) {
        this.httpServer = httpServer;
        this.restrictedCharset = restrictedCharset;
        this.appContext = appContext;
        this.port = httpServer.getAddress().getPort();
        if (webjarsSupportEnabled) {
            httpServer.createContext(this.appContext + "webjars", (ex -> resourceFolder(ex, "META-INF/resources/webjars", null)));
        }
        httpServer.createContext(this.appContext, (ex -> {
            resourceFolder(ex, "static/", externalFolderCanonical);
        }));
        this.defaultRender = defaultRender;
        this.gson = gson;

    }

    public PinServer on(String method, String path, PinHandler pinHandler, PinRender pinRender) {
        String fullPath = PinUtils.removeTrailingSlash(appContext + path);
        //This is needed because the way httpServer handles contexts. If it has a my-web-app context, said context
        //will NOT handle a my-web-app/users/1234 request, so I need to register a my-web-app/users context
        //So a unique my-web-app may (and probably will) internally have several contexts registered in the httpServer
        String maximalPathValidAsContext = PinUtils.maximalPathValidAsContext(fullPath);

        PinAdapter pinAdapter = adaptersByPath.get(maximalPathValidAsContext);
        if (pinAdapter != null) {
            pinAdapter.put(method, fullPath, pinHandler, pinRender);
        } else {
            pinAdapter = new PinAdapter(method, fullPath, pinHandler, pinRender, gson);
            httpServer.createContext(maximalPathValidAsContext, pinAdapter);
            adaptersByPath.put(maximalPathValidAsContext, pinAdapter);
        }
        return this;
    }

    public PinServer onGet(String path, PinResponse pinResponse, String pinRenderType) {
        return on("GET", path, ex -> pinResponse, this.findRender(pinRenderType));
    }

    public PinServer onGet(String path, PinHandler pinHandler, String pinRenderType) {
        return on("GET", path, pinHandler, this.findRender(pinRenderType));
    }

    public PinServer onGet(String path, PinResponse pinResponse) {
        return on("GET", path, ex -> pinResponse, defaultRender);
    }

    public PinServer onGet(String path, PinHandler pinHandler) {
        return on("GET", path, pinHandler, defaultRender);
    }

    public PinServer onPut(String path, PinResponse pinResponse, String pinRenderType) {
        return on("PUT", path, ex -> pinResponse, this.findRender(pinRenderType));
    }

    public PinServer onPut(String path, PinHandler pinHandler, String pinRenderType) {
        return on("PUT", path, pinHandler, this.findRender(pinRenderType));
    }

    public PinServer onPut(String path, PinResponse pinResponse) {
        return on("PUT", path, ex -> pinResponse, defaultRender);
    }

    public PinServer onPut(String path, PinHandler pinHandler) {
        return on("PUT", path, pinHandler, defaultRender);
    }

    public PinServer onPost(String path, PinResponse pinResponse, String pinRenderType) {
        return on("POST", path, ex -> pinResponse, this.findRender(pinRenderType));
    }

    public PinServer onPost(String path, PinHandler pinHandler, String pinRenderType) {
        return on("POST", path, pinHandler, this.findRender(pinRenderType));
    }

    public PinServer onPost(String path, PinResponse pinResponse) {
        return on("POST", path, ex -> pinResponse, defaultRender);
    }

    public PinServer onPost(String path, PinHandler pinHandler) {
        return on("POST", path, pinHandler, defaultRender);
    }

    public PinServer onDelete(String path, PinResponse pinResponse, String pinRenderType) {
        return on("DELETE", path, ex -> pinResponse, this.findRender(pinRenderType));
    }

    public PinServer onDelete(String path, PinHandler pinHandler, String pinRenderType) {
        return on("DELETE", path, pinHandler, this.findRender(pinRenderType));
    }

    public PinServer onDelete(String path, PinResponse pinResponse) {
        return on("DELETE", path, ex -> pinResponse, defaultRender);
    }

    public PinServer onDelete(String path, PinHandler pinHandler) {
        return on("DELETE", path, pinHandler, defaultRender);
    }

    private void resourceFolder(HttpExchange ex, String resourceFolder, File externalFolder) throws IOException {

        String filename = ex.getRequestURI().getPath().replaceFirst("\\Q" + ex.getHttpContext().getPath() + "\\E", "");
        if (filename.trim().length() == 0) {
            filename = "index.html";
        }

        String mimeType = PinMimeType.fromFileName(filename);
        ex.getResponseHeaders().add(PinContentType.CONTENT_TYPE, mimeType);

        if (!"GET".equals(ex.getRequestMethod())) {
            LOG.error("Error trying to access '{}', wrong method '{}'", ex.getRequestURI().getPath(),
                    ex.getRequestMethod());
            ex.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
            ex.getResponseBody().write(("Error trying to access '" + ex.getRequestURI().getPath() +"', wrong method '" + ex.getRequestMethod() +"'")
                    .getBytes(StandardCharsets.UTF_8));
            ex.close();
            return;
        }

        try (InputStream is = findInputStream(resourceFolder, externalFolder, filename)) {

            if (is == null) {
                ex.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
                ex.getResponseBody().write(("File '" + filename + "' not found")
                        .getBytes(StandardCharsets.UTF_8));
                LOG.warn("File not found for request uri '{}'", ex.getRequestURI().getPath());
            } else {

                ex.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

                PinUtils.copy(is, ex.getResponseBody());
            }
        } catch (Exception e) {
            LOG.error("Error on request uri '{}'", ex.getRequestURI().getPath(), e);
            ex.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
        }
        ex.close();
    }

    private InputStream findInputStream(String resourceFolder, File externalFolder, String filename)
            throws IOException {
        if (externalFolder != null) {
            File file = new File(externalFolder, filename).getCanonicalFile();
            if (file.getAbsolutePath().indexOf(externalFolder.getAbsolutePath()) != 0) {
                LOG.error("Error on filename '{}', directory traversal attack", filename);
                //DO NOT return a more specific message, give no clues about traversal attack being detected
                //should look like a standard file not found from the outside
                return null;
            }
            if (file.exists()) {
                return new FileInputStream(file);
            }
        }

        return PinServer.class.getClassLoader().getResourceAsStream(resourceFolder + filename);

    }

    public PinServer start() {
        String protocol = httpServer.getClass().getSimpleName().equals("HttpsServerImpl") ? "https" : " http";
        LOG.debug("Starting as {}://localhost:{}{}", protocol, port, appContext);
        httpServer.start();
        LOG.info("Started as {}://localhost:{}{}", protocol, port, appContext);
        return this;
    }

    public PinServer stop(int seconds) {
        String protocol = httpServer.getClass().getSimpleName().equals("HttpsServerImpl") ? "https" : " http";
        LOG.debug("Stopping {}://localhost:{}{} in about {} seconds", protocol, port, appContext, seconds);
        httpServer.stop(seconds);
        LOG.info("Stopped {}://localhost:{}{}", protocol, port, appContext);
        return this;
    }

    public HttpServer raw() {
        return httpServer;
    }

    public void registerRender(PinRender pinRender) {
        rendersByType.put(pinRender.getType(), pinRender);
    }

    public PinRender findRender(String pinRenderType) {
        if (pinRenderType == null) {
            return defaultRender;
        }

        if (rendersByType.containsKey(pinRenderType)) {
            return rendersByType.get(pinRenderType);
        }

        return defaultRender;

    }
}
