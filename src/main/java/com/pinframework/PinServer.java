package com.pinframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pinframework.requestmatcher.PinRouteRequestMatcher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import sun.net.httpserver.HttpsServerImpl;

@SuppressWarnings("restriction")
public class PinServer {

	private static final Logger LOG = LoggerFactory.getLogger(PinServer.class);

	/**
	 * will be / or /something/
	 */
	private final String appContext;
	private final HttpServer httpServer;
	private final int port;
	private final boolean restrictedCharset;

	private PinRedirectHttpHandler redirectHttpHandler;

	PinServer(HttpServer httpServer, boolean restrictedCharset, String appContext, boolean webjarsSupportEnabled, 
			boolean uploadSupportEnabled, File externalFolderCanonical, Gson gsonParser) {
		this.httpServer = httpServer;
		this.restrictedCharset = restrictedCharset;
		this.appContext = appContext;
		this.port = httpServer.getAddress().getPort();
		this.redirectHttpHandler = new PinRedirectHttpHandler(appContext, externalFolderCanonical, gsonParser, uploadSupportEnabled);
		httpServer.createContext(appContext, redirectHttpHandler);
		if (webjarsSupportEnabled) {
			httpServer.createContext(this.appContext + "webjars", (ex -> {
				resourceFolder(ex, "META-INF/resources/webjars", null);
			}));
		}
	}
	
	public PinServer on(PinRequestMatcher requestMatcher, PinHandler handler){
		LOG.info("{}", requestMatcher);
		redirectHttpHandler.on(requestMatcher, handler);
		return this;
	}
	public PinServer on(String method, String route, PinHandler handler){
		PinRouteRequestMatcher routeRequestMatcher = new PinRouteRequestMatcher(method.toUpperCase(Locale.ENGLISH), route, appContext);
		return on(routeRequestMatcher, handler);
	}
	public PinServer onGet(String route, PinHandler handler){
		PinRouteRequestMatcher routeRequestMatcher = new PinRouteRequestMatcher("GET", route, appContext);
		return on(routeRequestMatcher, handler);
	}
	public PinServer onPost(String route, PinHandler handler){
		PinRouteRequestMatcher routeRequestMatcher = new PinRouteRequestMatcher("POST", route, appContext);
		return on(routeRequestMatcher, handler);
	}
	

	private void resourceFolder(HttpExchange ex, String resourceFolder, File externalFolder) throws IOException {
		if (!"GET".equals(ex.getRequestMethod())) {
			LOG.error("Error trying to access '{}', wrong method '{}'", ex.getRequestURI().getPath(),
					ex.getRequestMethod());
			ex.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			ex.close();
			return;
		}
		String filename = ex.getRequestURI().getPath().replaceFirst("\\Q" + ex.getHttpContext().getPath() + "\\E", "");
		if (filename == null || filename.trim().length() == 0) {
			filename = "index.html";
		}

		try (InputStream is = findInputStream(resourceFolder, externalFolder, filename)) {

			if (is == null) {
				ex.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
				ex.getResponseBody().write(("File '" + filename + "' not found in '" + resourceFolder + "'")
						.getBytes(StandardCharsets.UTF_8));
				LOG.error("File not found on request uri '{}'", ex.getRequestURI().getPath());
			} else {
				String mimeType = PinMimeType.fromFileName(filename);
				ex.getResponseHeaders().add(PinMimeType.CONTENT_TYPE, mimeType);
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
				return null;
			}
			if (file.exists()) {
				return new FileInputStream(file);
			}
		}

		return PinServer.class.getClassLoader().getResourceAsStream(resourceFolder + filename);

	}

	public PinServer start() {
		String protocol = httpServer instanceof HttpsServerImpl ? "https" : " http";
		LOG.debug("Starting as " + protocol + "://localhost:" + port + appContext);
		httpServer.start();
		LOG.info("Started as  " + protocol + "://localhost:" + port + appContext);
		return this;
	}

	public PinServer stop(int seconds) {
		LOG.debug("Stopping https://localhost:" + port + appContext + " in about "  + seconds  + " seconds");
		httpServer.stop(seconds);
		LOG.info("Stopped https://localhost:" + port + appContext);
		return this;
	}

	public HttpServer raw() {
		return httpServer;
	}

}
