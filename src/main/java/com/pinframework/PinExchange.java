package com.pinframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinFileUploadRuntimeException;
import com.pinframework.exceptions.PinRuntimeException;
import com.sun.net.httpserver.HttpExchange;

//TODO: separar en exchange y exchange builder
public class PinExchange {

    private final HttpExchange httpExchange;
    private Map<String, List<String>> queryParams;
    private Map<String, Object> postParams;
    private Map<String, String> pathParams;
    private Map<String, FileItem> fileParams;
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
     * Only actual files are returned. Extra values are present in getPostParams
     *
     * @return
     */
    public Map<String, FileItem> getFileParams() {
        if (fileParams == null) {
            if (!isMultipart()) {
                // TODO: log error
                fileParams = Collections.emptyMap();
            } else {
                DiskFileItemFactory d = new DiskFileItemFactory();
                ServletFileUpload up = new ServletFileUpload(d);
                try {
                    List<FileItem> fileItems = up.parseRequest(new PinHttpHandlerRequestContext(httpExchange));
                    fileParams = Collections.unmodifiableMap(fileItems.stream().filter(fi -> !fi.isFormField())
                            .collect(Collectors.toMap(fi -> fi.getFieldName(), Function.identity())));
                    postParams = Collections.unmodifiableMap(fileItems.stream().filter(fi -> fi.isFormField())
                            .collect(Collectors.toMap(fi -> fi.getFieldName(), fi -> utf8Value(fi))));
                } catch (FileUploadException e) {
                    throw new PinFileUploadRuntimeException(e);
                }
            }
        }
        return fileParams;
    }

    private String utf8Value(FileItem fi) {
        try {
            return fi.getString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            //TODO: log error
            return fi.getString();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPostParams() {
        if (postParams == null) {
            if (isMultipart()) {
                getFileParams(); // just because getFileParams should be
                // executed first
            } else {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    PinUtils.copy(httpExchange.getRequestBody(), out);
                    String contentType = getRequestContentTypeParsed();
                    if (contentType == null) {
                        // TODO: log error
                    } else if ("application/json".equals(contentType)) {
                        // that's angular encoding by default
                        String json = out.toString(StandardCharsets.UTF_8);
                        postParams = gson.fromJson(json, HashMap.class);
                    } else if ("application/x-www-form-urlencoded".equals(contentType)) {
                        String postData = URLDecoder.decode(out.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        Map<String, List<String>> splitQuery = PinUtils.splitQuery(postData);
                        postParams = splitQuery.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                    }

                } catch (IOException e) {
                    //will be catch in main PinAdapter
                    throw new PinRuntimeException("Unexpected error parsing post params", e);
                }
            }
        }
        return postParams;
    }

    private boolean isMultipart() {
        String contentType = getRequestContentTypeParsed();
        return "multipart/form-data".equals(contentType) || "multipart/mixed".equals(contentType);
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
}
