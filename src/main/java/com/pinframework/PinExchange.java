package com.pinframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.pinframework.converter.PinEnumParamConverter;
import com.pinframework.converter.PinLocalDateParamConverter;
import com.pinframework.converter.PinLocalDateTimeParamConverter;
import com.pinframework.converter.PinLongParamConverter;
import com.pinframework.converter.PinParamConverter;
import com.pinframework.converter.PinZonedDateTimeParamConverter;
import com.pinframework.exceptions.PinBadRequestException;
import com.pinframework.exceptions.PinRuntimeException;
import com.sun.net.httpserver.HttpExchange;

public class PinExchange {

    private static final Logger LOG = LoggerFactory.getLogger(PinExchange.class);

    private final HttpExchange httpExchange;
    private Map<String, String> pathParams;
    private Map<String, List<String>> queryParams;
    private Map<String, Object> postParams;
    private Map<String, List<String>> formParams;
    private Map<String, List<FileItem>> fileParams;
    private boolean streamParsed = false;
    private final List<String> pathParamNames;
    private final Gson gson;

    private final PinLongParamConverter longParamConverter = new PinLongParamConverter();
    private final PinLocalDateParamConverter localDateParamConverter = new PinLocalDateParamConverter();
    private final PinLocalDateTimeParamConverter localDateTimeParamConverter = new PinLocalDateTimeParamConverter();
    private final PinZonedDateTimeParamConverter zonedDateTimeParamConverter = new PinZonedDateTimeParamConverter();

    public PinExchange(HttpExchange httpExchange, List<String> pathParamNames, Gson gson) {
        this.httpExchange = httpExchange;
        this.pathParamNames = pathParamNames;
        this.gson = gson;
    }

    public HttpExchange raw() {
        return httpExchange;
    }

    public Map<String, String> getPathParams() {
        if (pathParams == null) {
            pathParams = Collections.unmodifiableMap(PinUtils.splitPath(httpExchange.getRequestURI().getPath(),
                    httpExchange.getHttpContext().getPath(), pathParamNames));
        }
        return pathParams;
    }

    public Map<String, List<String>> getQueryParams() {
        if (queryParams == null) {
            queryParams = Collections
                    .unmodifiableMap(PinUtils.splitQuery(this.httpExchange.getRequestURI().getQuery()));
        }
        return queryParams;
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
     * Use this method if the object is not a generic
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getBodyAs(Class<T> clazz) {
        return getBodyInternal(s -> gson.fromJson(s, clazz));
    }

    /**
     * Parses the body as json into a new Object of class clazz<br>
     * Can only be called once
     * Will ignore the Content type header
     * Use this method if the object itself is generic
     * Type
     *
     * @param <T>     the type of the desired object
     * @param typeOfT The specific genericized type of src. You can obtain this type by using the
     *                {@link com.google.gson.reflect.TypeToken} class. For example, to get the type for
     *                {@code Collection<Foo>}, you should use:
     *                <pre>
     *                                                                                                                         Type typeOfT = new TypeToken&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
     *                                                                                                                         </pre>
     * @return an object of type T from the string. Returns {@code null} if {@code json} is {@code null}
     * or if {@code json} is empty.
     */
    public <T> T getBodyAs(Type typeOfT) {
        return getBodyInternal(s -> gson.fromJson(s, typeOfT));
    }

    private <T> T getBodyInternal(Function<String, T> gsonParser) {
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
        String json = out.toString(StandardCharsets.UTF_8);
        streamParsed = true;
        T obj;
        try {
            obj = gsonParser.apply(json);
            postParams = Collections.emptyMap();
            fileParams = Collections.emptyMap();
            formParams = Collections.emptyMap();
        } catch (JsonParseException jpe) {
            throw new PinBadRequestException(jpe.getMessage(), jpe);
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
            String bestEffort = fi.getString();
            LOG.warn("Trying to parse file item as UTF-8, as a best effort {} was returned", bestEffort, e);
            return bestEffort;
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

    /*
     * path param
     */

    public String getPathParam(String paramName) {
        return getPathParams().get(paramName);
    }

    public <T> T getPathParamConverted(String paramName, PinParamConverter<T> pinParamConverter) {
        String paramValue = getPathParams().get(paramName);
        if (paramValue == null) {
            return null;
        }
        return pinParamConverter.convert(paramName, paramValue);
    }

    public Long getPathParamAsLong(String paramName) {
        return getPathParamConverted(paramName, longParamConverter);
    }

    public <E extends Enum<E>> E getPathParamAsEnum(String paramName, Class<E> enumClass) {
        return getPathParamConverted(paramName, new PinEnumParamConverter<>(enumClass));
    }

    public LocalDate getPathParamAsLocalDate(String paramName) {
        return getPathParamConverted(paramName, localDateParamConverter);
    }

    public LocalDateTime getPathParamAsLocalDateTime(String paramName) {
        return getPathParamConverted(paramName, localDateTimeParamConverter);
    }

    public ZonedDateTime getPathParamAsZonedDateTime(String paramName) {
        return getPathParamConverted(paramName, zonedDateTimeParamConverter);
    }

    /*
     * query param
     */

    /**
     * @param paramName
     * @return the first value for that param name or null if none is present
     */
    public String getQueryParamFirst(String paramName) {
        List<String> stringList = getQueryParams().get(paramName);
        return stringList == null || stringList.isEmpty() ? null : stringList.get(0);
    }

    public <T> T getQueryParamFirstConverted(String paramName, PinParamConverter<T> pinParamConverter) {
        String paramValue = getQueryParamFirst(paramName);
        if (paramValue == null) {
            return null;
        }
        return pinParamConverter.convert(paramName, paramValue);
    }

    public <T> List<T> getQueryParamAsConvertedList(String paramName, PinParamConverter<T> pinParamConverter) {
        List<String> stringList = getQueryParams().get(paramName);
        if (stringList == null) {
            return null;
        }
        return stringList.stream().map(string -> pinParamConverter.convert(paramName, string)).collect(Collectors.toList());
    }

    public Long getQueryParamFirstAsLong(String paramName) {
        return getQueryParamFirstConverted(paramName, longParamConverter);
    }

    public List<Long> getQueryParamAsLongList(String paramName) {
        return getQueryParamAsConvertedList(paramName, longParamConverter);
    }

    public <E extends Enum<E>> E getQueryParamFirstAsEnum(String paramName, Class<E> enumClass) {
        return getQueryParamFirstConverted(paramName, new PinEnumParamConverter<>(enumClass));
    }

    public <E extends Enum<E>> List<E> getQueryParamAsEnumList(String paramName, Class<E> enumClass) {
        return getQueryParamAsConvertedList(paramName, new PinEnumParamConverter<>(enumClass));
    }

    public LocalDate getQueryParamFirstAsLocalDate(String paramName) {
        return getQueryParamFirstConverted(paramName, localDateParamConverter);
    }

    public List<LocalDate> getQueryParamAsLocalDateList(String paramName) {
        return getQueryParamAsConvertedList(paramName, localDateParamConverter);
    }

    public LocalDateTime getQueryParamFirstAsLocalDateTime(String paramName) {
        return getQueryParamFirstConverted(paramName, localDateTimeParamConverter);
    }

    public List<LocalDateTime> getQueryParamAsLocalDateTimeList(String paramName) {
        return getQueryParamAsConvertedList(paramName, localDateTimeParamConverter);
    }

    public ZonedDateTime getQueryParamFirstAsZonedDateTime(String paramName) {
        return getQueryParamFirstConverted(paramName, zonedDateTimeParamConverter);
    }

    public List<ZonedDateTime> getQueryParamAsZonedDateTimeList(String paramName) {
        return getQueryParamAsConvertedList(paramName, zonedDateTimeParamConverter);
    }

    /*
     * form param
     */

    /**
     * @param paramName
     * @return the first value for that param name or null if none is present
     */
    public String getFormParamFirst(String paramName) {
        List<String> stringList = getFormParams().get(paramName);
        return stringList == null || stringList.isEmpty() ? null : stringList.get(0);
    }

    public <T> T getFormParamFirstConverted(String paramName, PinParamConverter<T> pinParamConverter) {
        String paramValue = getFormParamFirst(paramName);
        if (paramValue == null) {
            return null;
        }
        return pinParamConverter.convert(paramName, paramValue);
    }

    public <T> List<T> getFormParamAsConvertedList(String paramName, PinParamConverter<T> pinParamConverter) {
        List<String> stringList = getFormParams().get(paramName);
        if (stringList == null) {
            return null;
        }
        return stringList.stream().map(string -> pinParamConverter.convert(paramName, string)).collect(Collectors.toList());
    }

    public Long getFormParamFirstAsLong(String paramName) {
        return getFormParamFirstConverted(paramName, longParamConverter);
    }

    public List<Long> getFormParamAsLongList(String paramName) {
        return getFormParamAsConvertedList(paramName, longParamConverter);
    }

    public <E extends Enum<E>> E getFormParamFirstAsEnum(String paramName, Class<E> enumClass) {
        return getFormParamFirstConverted(paramName, new PinEnumParamConverter<>(enumClass));
    }

    public <E extends Enum<E>> List<E> getFormParamAsEnumList(String paramName, Class<E> enumClass) {
        return getFormParamAsConvertedList(paramName, new PinEnumParamConverter<>(enumClass));
    }

    public LocalDate getFormParamFirstAsLocalDate(String paramName) {
        return getFormParamFirstConverted(paramName, localDateParamConverter);
    }

    public List<LocalDate> getFormParamAsLocalDateList(String paramName) {
        return getFormParamAsConvertedList(paramName, localDateParamConverter);
    }

    public LocalDateTime getFormParamFirstAsLocalDateTime(String paramName) {
        return getFormParamFirstConverted(paramName, localDateTimeParamConverter);
    }

    public List<LocalDateTime> getFormParamAsLocalDateTimeList(String paramName) {
        return getFormParamAsConvertedList(paramName, localDateTimeParamConverter);
    }

    public ZonedDateTime getFormParamFirstAsZonedDateTime(String paramName) {
        return getFormParamFirstConverted(paramName, zonedDateTimeParamConverter);
    }

    public List<ZonedDateTime> getFormParamAsZonedDateTimeList(String paramName) {
        return getFormParamAsConvertedList(paramName, zonedDateTimeParamConverter);
    }


}
