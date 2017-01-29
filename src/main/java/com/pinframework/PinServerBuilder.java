package com.pinframework;

import com.google.gson.Gson;
import com.pinframework.exception.PinInitializationException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fluent builder for PinServer. Error will be caught ASAP, if possible in the setter, if not on
 * build method
 */
public class PinServerBuilder {

  private static final Pattern RESTRICTED_APP_CONTEXT_PATTERN =
      Pattern.compile("/[a-z0-9\\-\\.]*/");

  public static final int DEFAULT_PORT = 9999;
  public static final boolean DEFAULT_RESTRICTED_CHARSET = false;
  public static final boolean DEFAULT_WEBJARS_SUPPORT_ENABLED = true;
  public static final boolean DEFAULT_WEBJARS_PREFER_MINIFIED = false;
  public static final boolean DEFAULT_UPLOAD_SUPPORT_ENABLED = false;

  private static final Logger LOG = LoggerFactory.getLogger(PinServerBuilder.class);

  private int port = DEFAULT_PORT;
  private boolean restrictedCharset = DEFAULT_RESTRICTED_CHARSET;
  private int maxBacklog = 0;
  private String appContext = "";
  private boolean webjarsSupportEnabled = DEFAULT_WEBJARS_SUPPORT_ENABLED;
  private boolean webjarsPreferMinified = DEFAULT_WEBJARS_PREFER_MINIFIED;
  private boolean uploadSupportEnabled = DEFAULT_UPLOAD_SUPPORT_ENABLED;
  private String externalFolder = null;
  private Executor executor = Executors.newFixedThreadPool(10);
  private boolean httpsSupportEnabled = false;
  private Gson gsonParser = null;
  // TODO: authenticator

  /**
   * A valid port value is between 0 and 65535. A port number of zero will let the system pick up an
   * ephemeral port in a bind operation<br>
   * Default 9999
   * 
   * @param port The port, default 9999
   * @return this instance so you can keep building
   */
  public PinServerBuilder port(int port) {
    if (port < 0 || port > 65535) {
      LOG.error("A valid port value is between 0 and 65535");
      throw new PinInitializationException("A valid port value is between 0 and 65535");
    }
    this.port = port;
    return this;
  }

  /**
   * If true Pin only allows
   * <ol>
   * <li>a to-z (lowercase)</li>
   * <li>0 to 9</li>
   * <li>- (minus)</li>
   * <li>. (dot)</li>
   * </ol>
   * in appContext and routes<br>
   * If false, appContext and routes can have any chars<br>
   * Default false
   * 
   * @param restrictedCharset true to allow only a-z, 0 to 9, minus and dot, default false
   * @return this instance so you can keep building
   */
  public PinServerBuilder restrictedCharset(boolean restrictedCharset) {
    this.restrictedCharset = restrictedCharset;
    return this;
  }

  /**
   * A common root path for everything exposed in this server<br>
   * So your application can expose itself in localhost:9999/app-context/index.html and every other
   * route starts with /app-context/<br/>
   * Back in the day when servers were shared between applications it was more popular<br>
   * Default "", your application will be in localhost:9999/
   * 
   * @param appContext root path for everything exposed in this server. Do NOT start or end with /
   * @return this instance so you can keep building
   */
  public PinServerBuilder appContext(String appContext) {
    if (appContext != null) {
      if (appContext.startsWith("/")) {
        throw new PinInitializationException(
            "appContext should not start with /. Will be added automatically");
      }
      if (appContext.endsWith("/")) {
        throw new PinInitializationException(
            "appContext should not end with /. Will be added automatically");
      }
    }
    this.appContext = appContext;
    return this;
  }

  /**
   * Enable support for webjars. Webjars will be available in <br>
   * localhost:9999/app-context/webjars/library/version/whatever (Example, <br>
   * localhost:9999/app-context/webjars/angularjs/1.5.6/angular.min.js<br>
   * 
   * @param webjarsSupportEnabled enable support for webjars
   * @return this instance so you can keep building, default true
   */
  public PinServerBuilder webjarsSupportEnabled(boolean webjarsSupportEnabled) {
    this.webjarsSupportEnabled = webjarsSupportEnabled;
    return this;
  }

  /**
   * Enable automatic minification for webjars. <br>
   * webjarsSupportEnabled should be true <br>
   * Webjars will be available in <br>
   * Pin will try to add .min in every file, so a request for<br>
   * angular.js<br>
   * becomes<br>
   * angular.min.js<br>
   * If angular.min.js is not present, will be back to angular.js<br>
   * Typically enabled in production environments, disabled in development environments<br>
   * 
   * @param webjarsPreferMinified use minified resource when available, default false
   * @return this instance so you can keep building
   */
  public PinServerBuilder webjarsPreferMinified(boolean webjarsPreferMinified) {
    this.webjarsPreferMinified = webjarsPreferMinified;
    return this;
  }

  /**
   * Enable support for upload.<br>
   * For this to work you need to have commons-fileupload in you classpath
   * 
   * @param uploadSupportEnabled true for support file upload, default false
   * @return this instance so you can keep building
   */
  public PinServerBuilder uploadSupportEnabled(boolean uploadSupportEnabled) {
    this.uploadSupportEnabled = uploadSupportEnabled;
    return this;
  }

  /**
   * External folder for static files. If there is a file with the same name and path in static
   * resource folder and external folder the external file is used<br>
   * External folder should be an existing readable directory<br>
   * Default null, no external folder is used
   * 
   * @param externalFolder full path for external folder, default null, no external folder
   * @return this instance so you can keep building
   */

  public PinServerBuilder externalFolder(String externalFolder) {
    this.externalFolder = externalFolder;
    return this;
  }

  /**
   * Maximum number of queued incoming connections to allow on the listening socket. Queued TCP
   * connections exceeding this limit may be rejected by the TCP implementation.<br>
   * Default 0, no maximum
   * 
   * @param maxBacklog Maximum number of queued incoming connections to allow
   * @return this instance so you can keep building
   */
  public PinServerBuilder maxBacklog(int maxBacklog) {
    if (maxBacklog < 0) {
      throw new PinInitializationException(
          "Invalid maxBacklog " + maxBacklog + ". Valid maxBacklog are 0 or positive");
    }
    this.maxBacklog = maxBacklog;
    return this;
  }

  /**
   * All HTTP requests are handled in tasks given to the executor.<br>
   * Default a FixedThreadPool with 10 threads
   * 
   * @param executor an executor to handle every task
   * @return this instance so you can keep building
   */
  public PinServerBuilder executor(Executor executor) {
    this.executor = executor;
    return this;
  }

  /**
   * A Gson instance used to parse json objects.<br>
   * Default a Gson instance with support for
   * <ol>
   * <li>java.time.LocalDateTime as yyyyMMddTHH:mm:ss</li>
   * <li>java.time.ZonedDateTime as yyyyMMddTHH:mm:ss+OFF</li>
   * </ol>
   * 
   * @param gsonParser A Gson instance used to parse json objects
   * @return this instance so you can keep building
   */
  public PinServerBuilder gsonParser(Gson gsonParser) {
    this.gsonParser = gsonParser;
    return this;
  }

  /**
   * Enable https support. Default false
   * 
   * @param httpsSupportEnabled support for https, default false
   * @return this instance so you can keep building
   */
  public PinServerBuilder httpsSupportEnabled(boolean httpsSupportEnabled) {
    this.httpsSupportEnabled = httpsSupportEnabled;
    return this;
  }

  /**
   * Builds a PinServer with the given configuration. Server is not started
   * 
   * @return The actual PinServer built
   */
  public PinServer build() {
    this.appContext =
        appContext == null || appContext.trim().length() == 0 ? "/" : "/" + appContext + "/";


    if (webjarsSupportEnabled == false && webjarsPreferMinified) {
      throw new PinInitializationException(
          "webjarsPreferMinimized is true but webjarsSupportEnabled is false. "
              + " Enable webjar support first");
    }


    if (restrictedCharset) {

      if (!RESTRICTED_APP_CONTEXT_PATTERN.matcher(appContext).matches()) {
        throw new PinInitializationException(
            "restrictedCharset is true, appContext must contain only small letters, numbers,"
                + " -  (minus) and . (dot)");
      }
    }

    File externalFolderCanonical = null;
    if (externalFolder != null) {
      try {
        externalFolderCanonical = new File(externalFolder).getCanonicalFile();
      } catch (Exception ioe) {
        throw new PinInitializationException(
            "Invalid externalFolder '" + externalFolder + "'. Can not get canonical representation",
            ioe);
      }
      if (!externalFolderCanonical.exists()) {
        throw new PinInitializationException(
            "Invalid externalFolder '" + externalFolder + "'. It does not exists");
      }
      if (!externalFolderCanonical.isDirectory()) {
        throw new PinInitializationException(
            "Invalid externalFolder '" + externalFolder + "'. It is not a folder");
      }
    }
    if (uploadSupportEnabled) {
      try {
        Class.forName("org.apache.commons.fileupload.FileItemFactory");
      } catch (Exception ex) {
        throw new PinInitializationException(
            "If uploadSupport is enabled  you need to have commons-fileupload in your classpath");
      }
    }

    if (gsonParser == null) {
      gsonParser = PinGson.getInstance();
    }

    InetSocketAddress address = new InetSocketAddress(port);
    HttpServer httpServer;
    try {
      httpServer = httpsSupportEnabled ? HttpsServer.create(address, maxBacklog)
          : HttpServer.create(address, maxBacklog);
    } catch (IOException ex) {
      throw new PinInitializationException("Can not create server", ex);
    }
    httpServer.setExecutor(executor);

    PinServer pinServer =
        new PinServer(httpServer, restrictedCharset, appContext, webjarsSupportEnabled,
            webjarsPreferMinified, uploadSupportEnabled, externalFolderCanonical, gsonParser);
    return pinServer;
  }

}
