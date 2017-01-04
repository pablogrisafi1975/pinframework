package com.pinframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

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
	private final Gson gson;

	private PinRedirectHandler redirectHandler;

	
	//TODO use default json/text/download render but allow to change... keep and pass instances around instead of static

	PinServer(HttpServer httpServer, boolean restrictedCharset, String appContext, boolean webjarsSupportEnabled, 
			boolean uploadSupportEnabled, File externalFolderCanonical, Gson gson) {
		this.httpServer = httpServer;
		this.restrictedCharset = restrictedCharset;
		this.appContext = appContext;
		this.port = httpServer.getAddress().getPort();
		this.redirectHandler = new PinRedirectHandler(gson, uploadSupportEnabled);
		this.gson = gson;
		httpServer.createContext(appContext, redirectHandler);
//		if (webjarsSupportEnabled) {
//			httpServer.createContext(this.appContext + "webjars", (ex -> {
//				resourceFolder(ex, "META-INF/resources/webjars", null);
//			}));
//		}
//		httpServer.createContext(this.appContext, (ex -> {
//			// File externalPath = new
//			// File("/home/pablogrisafi/workspaces/op-txfinder/op-txfinder-pin/external");
//			resourceFolder(ex, "static/", externalFolderCanonical);
//		}));
	}
	
	public PinServer on(PinRequestMatcher requestPredicate, PinHandler handler){
		redirectHandler.on(requestPredicate, handler);
		return this;
	}
	public PinServer onGet(String route, PinHandler handler){
		PinRouteRequestMatcher routeMatcher = new PinRouteRequestMatcher("GET", route, appContext);
		redirectHandler.on(routeMatcher, handler);
		return this;
	}
	
//	public PinServer getJson(String route, PinHandler handler){
//		PinRequestPredicate requestPredicate = new PinGetJsonRequestPredicate(route);
//		redirectHandler.on(requestPredicate, handler);
//		return this;
//	}
//	
//
//	
//	public PinServer on(String method, String path, PinHandler pinHandler) {
//		List<String> contextAndPathParameters = PinUtils.contextAndPathParameters(appContext, path);
//		String contextPath = contextAndPathParameters.get(0);
//		List<String> pathParameterNames = contextAndPathParameters.subList(1, contextAndPathParameters.size());
//		PinAdapter pinAdapter = adaptersByPath.get(contextPath);
//		if (pinAdapter != null) {
//			pinAdapter.put(method, pathParameterNames, pinHandler);
//		} else {
//			pinAdapter = new PinAdapter(method, pathParameterNames, pinHandler);
//			httpServer.createContext(contextPath, pinAdapter);
//			adaptersByPath.put(contextPath, pinAdapter);
//		}
//		return this;
//	}
//
//	public PinServer onGet(String path, PinHandler pinHandler) {
//		return on("GET", path, pinHandler);
//	}
//
//	public PinServer onPut(String path, PinHandler pinHandler) {
//		return on("PUT", path, pinHandler);
//	}
//
//	public PinServer onPost(String path, PinHandler pinHandler) {
//		return on("POST", path, pinHandler);
//	}
//
//	public PinServer onDelete(String path, PinHandler pinHandler) {
//		return on("DELETE", path, pinHandler);
//	}

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
