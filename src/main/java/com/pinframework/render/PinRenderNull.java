package com.pinframework.render;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.pinframework.PinRender;
import com.pinframework.PinRenderType;

public class PinRenderNull implements PinRender {

    @Override
    public String getType() {
        return PinRenderType.NULL;
    }

    @Override
    public void render(Object obj, OutputStream outputStream) throws IOException {
        //Server sent events do not render data on close
    }

    @Override
    public void changeHeaders(Map<String, List<String>> responseHeaders) {
        //Server sent events do not render data on close
    }

}
