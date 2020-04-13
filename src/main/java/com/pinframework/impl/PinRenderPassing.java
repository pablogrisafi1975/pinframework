package com.pinframework.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.pinframework.PinRender;
import com.pinframework.PinRenderType;

public class PinRenderPassing implements PinRender {

    @Override
    public String getType() {
        return PinRenderType.PASSING;
    }

    @Override
    public void render(Object obj, OutputStream outputStream) {
        if (obj != null) {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
            pw.write(obj.toString());
            pw.flush();
            pw.close();
        }
    }

}
