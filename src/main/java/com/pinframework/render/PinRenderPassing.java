package com.pinframework.render;

import java.io.BufferedWriter;
import java.io.IOException;

import com.pinframework.PinRenderType;

public class PinRenderPassing extends PinAbstractRender {

    @Override
    public String getType() {
        return PinRenderType.PASSING;
    }

    @Override
    protected String getNewContentType() {
        return null;
    }

    @Override
    protected void writeNonNullObject(BufferedWriter writer, Object obj) throws IOException {
        writer.write(obj.toString());
    }
}
