package com.pinframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinRuntimeException;
import com.sun.net.httpserver.HttpExchange;

public class PinExchange {

    private static final Logger LOG = LoggerFactory.getLogger(PinExchange.class);

    private final HttpExchange httpExchange;
    private Map<String, List<String>> queryParams;
    private Map<String, String> pathParams;
    private Map<String, Object> postParams;
    private Map<String, List<String>> formParams;
    private Map<String, List<FileItem>> fileParams;
    private boolean streamParsed = false;
    private final List<String> pathParamNames;
    private final Gson gson;

    public PinExchange(HttpExchange httpExchange, List<String> pathParamNames, Gson gson) {
        this.httpExchange = httpExchange;
        this.pathParamNames = pathParamNames;
        this.gson = gson;
    }

    public HttpExchange raw() {
        return httpExchange;
    }

    public Map<String, List<String>> getQueryParams() {
        if (queryParams == null) {
            queryParams = Collections
                    .unmodifiableMap(PinUtils.splitQuery(this.httpExchange.getRequestURI().getQuery()));
        }
        return queryParams;
    }

    public Map<String, String> getPathParams() {
        if (pathParams == null) {
            pathParams = Collections.unmodifiableMap(PinUtils.splitPath(httpExchange.getRequestURI().getPath(),
                    httpExchange.getHttpContext().getPath(), pathParamNames));
        }
        return pathParams;
    }

    /**
     * Only actual files are returned. Extra values are present in getFormDataParams
     *
     * @return a map from parameter name to FileItem
     */
    public Map<String, List<FileItem>> getFileParams() {
        if (!streamParsed) {
            parseStream();
        }
        return fileParams;
    }

    public Map<String, List<String>> getFormParams() {
        if (!streamParsed) {
            parseStream();
        }
        return formParams;
    }

    public Map<String, Object> getPostParams() {
        if (!streamParsed) {
            parseStream();
        }
        return postParams;
    }

    /**
     * Parses the body as json into a new Object of class clazz<br>
     * Can only be called once
     * Will ignore the Content type header
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getPostBodyAs(Class<T> clazz) {
        if (streamParsed) {
            throw new PinRuntimeException("Trying to parse the body twice");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PinUtils.copy(httpExchange.getRequestBody(), out);
        } catch (IOException e) {
            //will be catch in main PinAdapter
            throw new PinRuntimeException("Unexpected error reading input stream", e);
        }
        streamParsed = true;
        T obj;
        try {
            obj = gson.fromJson(out.toString(StandardCharsets.UTF_8), clazz);
            postParams = Collections.emptyMap();
            fileParams = Collections.emptyMap();
            formParams = Collections.emptyMap();
        } catch (JsonSyntaxException jse) {
            throw new PinBadRequestException(jse.getMessage(), jse);
        } catch (Exception ex) {
            throw new PinBadRequestException("Unexpected exception parsing json", ex);
        }
        return obj;
    }

    private void parseStream() {
        String contentType = getRequestContentType();
        if (contentType != null && contentType.contains("multipart")) {
            //I don't read the whole stream here, until the user really ask for it
            DiskFileItemFactory d = new DiskFileItemFactory();
            ServletFileUpload up = new ServletFileUpload(d);
            try {
                List<FileItem> fileItems = up.parseRequest(new PinHttpHandlerRequestContext(httpExchange));
                fileParams = Collections.unmodifiableMap(fileItems.stream()
                        .filter(fi -> !fi.isFormField())
                        .map(fi -> new AbstractMap.SimpleImmutableEntry<>(fi.getFieldName(), fi))
                        .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList()))));
                formParams = Collections.unmodifiableMap(fileItems.stream()
                        .filter(fi -> fi.isFormField())
                        .map(fi -> new AbstractMap.SimpleImmutableEntry<>(fi.getFieldName(), utf8Value(fi)))
                        .collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, LinkedHashMap::new,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList()))));
                postParams = Collections.emptyMap();
            } catch (FileUploadException fue) {
                throw new PinBadRequestException(fue.getMessage(), fue);
            } catch (Exception ex) {
                throw new PinBadRequestException("Unexpected exception parsing multipart body", ex);
            }
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                PinUtils.copy(httpExchange.getRequestBody(), out);
            } catch (IOException e) {
                //will be catch in main PinAdapter
                throw new PinRuntimeException("Unexpected error reading input stream", e);
            }
            if (contentType == null || contentType.contains("application/json")) {
                // that's angular encoding by default
                String json = out.toString(StandardCharsets.UTF_8);
                try {
                    postParams = gson.fromJson(json, HashMap.class);
                    fileParams = Collections.emptyMap();
                    formParams = Collections.emptyMap();
                } catch (JsonSyntaxException jse) {
                    throw new PinBadRequestException(jse.getMessage(), jse);
                } catch (Exception ex) {
                    throw new PinBadRequestException("Unexpected exception parsing json", ex);
                }
            } else if (contentType.contains("application/x-www-form-urlencoded")) {
                try {
                    String postData = URLDecoder.decode(out.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    Map<String, List<String>> splitQuery = PinUtils.splitQuery(postData);
                    formParams = splitQuery.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                    fileParams = Collections.emptyMap();
                    postParams = Collections.emptyMap();
                } catch (IllegalArgumentException iae) {
                    throw new PinBadRequestException(iae.getMessage(), iae);
                } catch (Exception ex) {
                    throw new PinBadRequestException("Unexpected exception parsing x-www-form-urlencoded", ex);
                }
            }

        }
        streamParsed = true;
    }

    private String utf8Value(FileItem fi) {
        try {
            return fi.getString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            //TODO: log error
            return fi.getString();
        }
    }

    public Map<String, List<String>> getRequestHeaders() {
        // only because headers class has restrictions
        return httpExchange.getRequestHeaders();
    }

    public Map<String, List<String>> getResponseHeaders() {
        // only because headers class has restrictions
        return httpExchange.getResponseHeaders();
    }

    /**
     * Full value of content type header, maybe something like
     * multipart/form-data; charset=utf-8; boundary="98279749896q696969696"
     *
     * @return
     */
    public String getRequestContentType() {
        return httpExchange.getRequestHeaders().getFirst(PinContentType.CONTENT_TYPE);
    }

    /**
     * Only the first part of the value of content type header<br>
     * , If header is multipart/form-data; charset=utf-8;
     * boundary="98279749896q696969696" getRequestContentTypeParsed() returns
     * multipart/form-data;
     *
     * @return
     */
    public String getRequestContentTypeParsed() {
        String contentType = getRequestContentType();
        return contentType == null ? null : contentType.split(";", -1)[0];
    }

    public void writeResponseContentType(String contentType) {
        PinUtils.put(raw().getResponseHeaders(), PinContentType.CONTENT_TYPE, contentType);
    }

    /**
     * This method will encode the file name, even if it has spaces or non-ascii chars<br/>
     * Also, will set the contentType to application/force-download
     *
     * @param fileName
     */
    public void writeDownloadFileName(String fileName) {
        writeResponseContentType(PinContentType.APPLICATION_FORCE_DOWNLOAD);
        PinUtils.put(raw().getResponseHeaders(), "Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\";");
    }

    public String getRequestAccept() {
        return httpExchange.getRequestHeaders().getFirst("Accept");
    }

    public String getPathParam(String paramName) {
        return getPathParams().get(paramName);
    }

    public Long getPathParamAsLong(String paramName) {
        String paramValue = getPathParam(paramName);
        Long value;
        try {
            value = Long.parseLong(paramValue);
        } catch (Exception ex) {
            throw new PinBadRequestException(paramName, paramValue, Long.class.getSimpleName(), ex);
        }
        return value;
    }

    /**
     * @param paramName
     * @return the first value for that param name or null if none is present
     */
    public String getQueryParamFirst(String paramName) {
        List<String> stringList = getQueryParams().get(paramName);
        return stringList == null || stringList.isEmpty() ? null : stringList.get(0);
    }

}
