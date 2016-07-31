package com.pinframework;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.RequestContext;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class PinHttpHandlerRequestContext implements RequestContext {
    private HttpExchange http;

    public PinHttpHandlerRequestContext(HttpExchange http) {
          this.http = http;
    }

    @Override
    public String getCharacterEncoding() {
          return PinUtils.UTF_8; 
    }

    @Override
    public int getContentLength() {
          return 0; 
    }


	@Override
    public String getContentType() {
          return http.getRequestHeaders().getFirst(PinMimeType.CONTENT_TYPE);
	}

    @Override
    public InputStream getInputStream() throws IOException {
          return http.getRequestBody();
    }
}
