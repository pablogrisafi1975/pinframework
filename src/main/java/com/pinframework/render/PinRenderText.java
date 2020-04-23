package com.pinframework.render;

import java.io.BufferedWriter;
import java.io.IOException;

import com.pinframework.PinContentType;
import com.pinframework.PinRenderType;

public class PinRenderText extends PinAbstractRender {

    @Override
    public String getType() {
        return PinRenderType.TEXT;
    }

    @Override
    protected String getNewContentType() {
        return PinContentType.TEXT_PLAIN_UTF8;
    }

    @Override
    protected void writeNonNullObject(BufferedWriter writer, Object obj) throws IOException {
        writer.write(obj.toString());
    }
}
