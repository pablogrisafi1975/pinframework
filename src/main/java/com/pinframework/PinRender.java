package com.pinframework;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface PinRender {
    //se podría armar un csv response , y hasta u xml/excel response
    void render(Object obj, OutputStream outputStream) throws Exception;

    String getType();

    default void changeHeaders(Map<String, List<String>> responseHeaders) {

    }
}
