package com.pinframework;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload.RequestContext;

import com.sun.net.httpserver.HttpExchange;

public class PinHttpHandlerRequestContext implements RequestContext {
    private HttpExchange http;

    public PinHttpHandlerRequestContext(HttpExchange http) {
        this.http = http;
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return http.getRequestHeaders().getFirst(PinContentType.CONTENT_TYPE);
    }

    @Override
    public InputStream getInputStream() {
        return http.getRequestBody();
    }
}
