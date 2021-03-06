package com.pinframework.response;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinContentType;
import com.pinframework.PinExchange;
import com.pinframework.PinUtils;
import com.pinframework.exceptions.PinRuntimeException;
import com.sun.net.httpserver.HttpExchange;

public class PinResponseSse {
    private final PrintWriter printWriter;

    private PinResponseSse(PinExchange pinExchange) {
        //super(HttpURLConnection.HTTP_OK, pinExchange, PinRenderNull.INSTANCE);
        HttpExchange httpExchange = pinExchange.raw();
        httpExchange.getResponseHeaders().add(PinContentType.CONTENT_TYPE, "text/event-stream");
        httpExchange.getResponseHeaders().add("CharacterEncoding", "UTF-8");
        try {
            httpExchange.sendResponseHeaders(200, 0);
            httpExchange.getResponseBody().flush();
            PinUtils.fullyRead(httpExchange.getRequestBody());
        } catch (IOException e) {
            throw new PinRuntimeException("Unexpected error sending SSE response", e);
        }
        this.printWriter = new PrintWriter(new OutputStreamWriter(httpExchange.getResponseBody(), StandardCharsets.UTF_8),
                false);
    }

    public static PinResponseSse of(PinExchange pinExchange) {
        return new PinResponseSse(pinExchange);
    }

    /**
     * @param data
     * @return true is sent without errors, false if couldn't send data
     */
    public boolean send(String data) {
        // TODO: multiline, id, json, comments
        String next = "data: " + data + "\n\n";
        printWriter.append(next);
        return !printWriter.checkError();
    }

    public boolean keepResponseOpen() {
        return true;
    }

}
