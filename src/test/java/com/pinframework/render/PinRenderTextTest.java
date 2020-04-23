package com.pinframework.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PinRenderTextTest {

    private final PinRenderText pinRenderText = new PinRenderText();

    @Test
    public void getType() {
        assertEquals("TEXT", pinRenderText.getType());
    }

    @Test
    public void whenRenderSomethingThenRender() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pinRenderText.render("whatever with ñ", outputStream);
        String rendered = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals("whatever with ñ", rendered);
    }

    @Test
    public void whenRenderNullThenJustClose() throws IOException {
        final OutputStream outputStream = mock(OutputStream.class);
        pinRenderText.render(null, outputStream);
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeHeaders() {
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        pinRenderText.changeHeaders(responseHeaders);
        assertEquals("text/plain; charset=utf-8", responseHeaders.get("Content-Type").get(0));
    }
}