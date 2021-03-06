package com.pinframework;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinInitializationException;
import com.pinframework.render.PinRenderFileDownload;
import com.pinframework.render.PinRenderHtml;
import com.pinframework.render.PinRenderJson;
import com.pinframework.render.PinRenderNull;
import com.pinframework.render.PinRenderPassing;
import com.pinframework.render.PinRenderText;
import com.pinframework.json.PinGsonBuilderFactory;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class PinServerBuilder {

    private static final Pattern APP_CONTEXT_INVALID_CHARS_PATTERN = Pattern.compile(".*([^a-z0-9\\-/]).*");

    private static final Logger LOG = LoggerFactory.getLogger(PinServerBuilder.class);

    private int port = 9999;
    private int maxBacklog = 0;
    private boolean restrictedCharset = true;
    private String appContext = "";
    private boolean webjarsSupportEnabled = true;
    private String externalFolder = null;
    private Executor executor = Executors.newFixedThreadPool(10);
    private boolean httpsSupportEnabled = false;
    private PinRender defaultRender = null; //if not set will be initialized before invoking the PinServer constructor
    private Gson gson = null;//if not set will be initialized before invoking the PinServer constructor using PinGsonBuilderFactory
    // TODO: incluir un authenticator

    /**
     * A valid port value is between 0 and 65535. A port number of zero will let
     * the system pick up an ephemeral port in a bind operation<br>
     * Default 9999
     *
     * @param port application port
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
     * If true Pin only allows a-to-z (lowercase), 0 to 9, - (minus) and . (dot)
     * appContext and routes<br>
     * If false, appContext and routes can have any chars<br>
     * Default true
     *
     * @param restrictedCharset
     * @return this instance so you can keep building
     */
    public PinServerBuilder restrictedCharset(boolean restrictedCharset) {
        this.restrictedCharset = restrictedCharset;
        return this;
    }

    /**
     * A common root path for every out in the server<br>
     * So your application can expose itself in
     * localhost:9999/app-context/index.html and every other route starts with
     * /app-context<br/>
     * Back in the day when servers were shared between applications it was more
     * popular<br>
     * Default "", your application will be in localhost:9999/
     *
     * @param appContext
     * @return this instance so you can keep building
     */
    public PinServerBuilder appContext(String appContext) {
        this.appContext = appContext;
        return this;
    }

    /**
     * Enable support for webjars. Webjars will be available in
     * localhost:9999/app-context/webjars/library/version/wahtever (Example,
     * localhost:9999/app-context/webjars/angularjs/1.5.6/angular.min.js<br>
     * Default true
     *
     * @param webjarsSupportEnabled
     * @return this instance so you can keep building
     */
    public PinServerBuilder webjarsSupportEnabled(boolean webjarsSupportEnabled) {
        this.webjarsSupportEnabled = webjarsSupportEnabled;
        return this;
    }

    /**
     * External folder for static files. If there is a file with the same name
     * and path in static resource folder and external folder the external file
     * is used<br>
     * External folder should be an existing readable directory<br>
     * Default null, no external folder is used
     *
     * @param externalFolder
     * @return this instance so you can keep building
     */

    public PinServerBuilder externalFolder(String externalFolder) {
        this.externalFolder = externalFolder;
        return this;
    }

    /**
     * Maximum number of queued incoming connections to allow on the listening
     * socket. Queued TCP connections exceeding this limit may be rejected by
     * the TCP implementation.<br>
     * Default 0, no maximum
     *
     * @param maxBacklog
     * @return this instance so you can keep building
     */
    public PinServerBuilder setMaxBacklog(int maxBacklog) {
        this.maxBacklog = maxBacklog;
        return this;
    }

    /**
     * All HTTP requests are handled in tasks given to the executor.<br>
     * Default a FixedThreadPool with 10 threads
     *
     * @param executor
     * @return this instance so you can keep building
     */
    public PinServerBuilder setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Enable https support<br>
     * Default false
     *
     * @param httpsSupportEnabled
     * @return this instance so you can keep building
     */
    public PinServerBuilder httpsSupportEnabled(boolean httpsSupportEnabled) {
        this.httpsSupportEnabled = httpsSupportEnabled;
        return this;
    }

    /**
     * The render that will be used if none is specified. <br>
     * Default an instance of PinRenderJson
     *
     * @param defaultRender
     * @return this instance so you can keep building
     */
    public PinServerBuilder defaultRender(PinRender defaultRender) {
        this.defaultRender = defaultRender;
        return this;
    }

    /**
     * A Gson instance that will be used to read and write JSON. <br>
     * Default value will be constructed with PinGsonBuilderFactory;
     *
     * @param gson
     * @return this instance so you can keep building
     */
    public PinServerBuilder gson(Gson gson) {
        this.gson = gson;
        return this;
    }

    public PinServer build() {
        this.appContext = appContext == null || appContext.trim().length() == 0 ? "/"
                : "/" + appContext.replaceAll("/", "") + "/";

        if (restrictedCharset && appContext != null) {
            Matcher matcher = APP_CONTEXT_INVALID_CHARS_PATTERN.matcher(appContext);
            if (matcher.matches()) {
                String invalid = matcher.group(1);
                throw new PinInitializationException(
                        "Invalid chars '" + invalid + "' in appContext. Valid chars are a to z, 0 to 9 and -");
            }
        }
        if (port < 0 || port > 65535) {
            throw new PinInitializationException("Invalid port " + port + ". Valid port are 0 to 65535");
        }
        if (maxBacklog < 0) {
            throw new PinInitializationException(
                    "Invalid maxBacklog " + maxBacklog + ". Valid maxBacklog are 0 or positive");
        }

        File externalFolderCanonical = null;
        if (externalFolder != null) {
            try {
                externalFolderCanonical = new File(externalFolder).getCanonicalFile();
            } catch (Exception ioe) {
                throw new PinInitializationException(
                        "Invalid externalFolder '" + externalFolder + "'. Can not get canonical representation", ioe);
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

        InetSocketAddress address = new InetSocketAddress(port);
        HttpServer httpServer;
        try {
            httpServer = httpsSupportEnabled ? HttpsServer.create(address, maxBacklog) : HttpServer.create(address, maxBacklog);
        } catch (IOException e) {
            throw new PinInitializationException(
                    "Can not create server", e);
        }
        httpServer.setExecutor(executor);

        if (gson == null) {
            gson = PinGsonBuilderFactory.make().create();
        }
        if (defaultRender == null) {
            defaultRender = new PinRenderJson(gson);
        }

        PinServer pinServer = new PinServer(httpServer, restrictedCharset, appContext, webjarsSupportEnabled, externalFolderCanonical,
                defaultRender, gson);

        pinServer.registerRender(defaultRender.getType().equals(PinRenderType.JSON) ? defaultRender : new PinRenderJson(gson));
        pinServer.registerRender(new PinRenderText());
        pinServer.registerRender(new PinRenderHtml());
        pinServer.registerRender(new PinRenderPassing());
        pinServer.registerRender(new PinRenderNull());
        pinServer.registerRender(new PinRenderFileDownload());

        return pinServer;
    }

}