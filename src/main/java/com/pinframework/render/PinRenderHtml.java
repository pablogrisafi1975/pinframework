package com.pinframework.render;

import java.io.BufferedWriter;
import java.io.IOException;

import com.pinframework.PinContentType;
import com.pinframework.PinRenderType;

public class PinRenderHtml extends PinAbstractRender {

    @Override
    public String getType() {
        return PinRenderType.HTML;
    }

    @Override
    protected String getNewContentType() {
        return PinContentType.TEXT_HTML_UTF8;
    }

    @Override
    protected void writeNonNullObject(BufferedWriter writer, Object obj) throws IOException {
        writer.write(obj.toString());
    }



}
