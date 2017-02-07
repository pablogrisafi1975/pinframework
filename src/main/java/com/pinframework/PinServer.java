package com.pinframework;

import com.google.gson.Gson;
import com.pinframework.constant.PinHeader;
import com.pinframework.httphandler.PinRedirectHttpHandler;
import com.pinframework.httphandler.PinWebjarsHttpHandler;
import com.pinframework.requestmatcher.PinRouteRequestMatcher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.httpserver.HttpsServerImpl;

public class PinServer {

  private static final Logger LOG = LoggerFactory.getLogger(PinServer.class);

  /**
   * will be / or /something/ .
   */
  public final String appContext;
  public final HttpServer httpServer;
  public final int port;
  public final boolean restrictedCharset;

  private PinRedirectHttpHandler redirectHttpHandler;

  PinServer(HttpServer httpServer, boolean restrictedCharset, String appContext,
      boolean webjarsSupportEnabled, boolean webjarsAutoMinimize, boolean uploadSupportEnabled,
      File externalFolderCanonical, Gson gsonParser) {
    this.httpServer = httpServer;
    this.restrictedCharset = restrictedCharset;
    this.appContext = appContext;
    this.port = httpServer.getAddress().getPort();
    this.redirectHttpHandler = new PinRedirectHttpHandler(appContext, externalFolderCanonical,
        gsonParser, uploadSupportEnabled);
    httpServer.createContext(appContext, redirectHttpHandler);
    // sadly, it does not work as localhost:8080
    if (appContext.length() > 1) {
      // this second context is needed just to handle /app-context (without the last /)
      httpServer.createContext(appContext.substring(0, appContext.length() - 1), new HttpHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
          exchange.getResponseHeaders().add(PinHeader.LOCATION, appContext);
          exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, 0);
          exchange.close();
        }
      });
    }
    if (webjarsSupportEnabled) {
      httpServer.createContext(this.appContext + "webjars",
          new PinWebjarsHttpHandler(webjarsAutoMinimize));
    }
  }

  public PinServer on(PinRequestMatcher requestMatcher, PinHandler handler) {
    LOG.info("{}", requestMatcher);
    redirectHttpHandler.on(requestMatcher, handler);
    return this;
  }

  public PinServer on(String method, String route, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher(method.toUpperCase(Locale.ENGLISH), route, appContext);
    return on(routeRequestMatcher, handler);
  }

  public PinServer on(String method, String route, String accept, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher(method.toUpperCase(Locale.ENGLISH), route, appContext, accept);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onGet(String route, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("GET", route, appContext);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onGet(String route, String accept, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("GET", route, appContext, accept);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onPost(String route, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("POST", route, appContext);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onPost(String route, String accept, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("POST", route, appContext, accept);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onPut(String route, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("PUT", route, appContext);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onPut(String route, String accept, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("PUT", route, appContext, accept);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onDelete(String route, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("DELETE", route, appContext);
    return on(routeRequestMatcher, handler);
  }

  public PinServer onDelete(String route, String accept, PinHandler handler) {
    PinRouteRequestMatcher routeRequestMatcher =
        new PinRouteRequestMatcher("DELETE", route, appContext, accept);
    return on(routeRequestMatcher, handler);
  }

  // TODO: before, onexception, onsuccess, after

  public PinServer start() {
    String protocol = httpServer instanceof HttpsServerImpl ? "https" : " http";
    LOG.debug("Starting as {}://localhost:{}{}", protocol, port, appContext);
    httpServer.start();
    LOG.debug("Started as {}://localhost:{}{}", protocol, port, appContext);
    return this;
  }

  public PinServer stop(int seconds) {
    String protocol = httpServer instanceof HttpsServerImpl ? "https" : " http";
    LOG.debug("Stopping as {}://localhost:{}{} in about {} seconds", protocol, port, appContext,
        seconds);
    httpServer.stop(seconds);
    LOG.debug("Stopped as {}://localhost:{}{}", protocol, port, appContext);
    return this;
  }

  public HttpServer raw() {
    return httpServer;
  }



}
