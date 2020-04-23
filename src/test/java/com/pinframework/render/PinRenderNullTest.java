package com.pinframework.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PinRenderNullTest {
    final PinRenderNull pinRenderNull = new PinRenderNull();

    @Test
    public void getType() {
        assertEquals("NULL", pinRenderNull.getType());
    }

    @Test
    public void whenRenderDoNothing() throws IOException {
        final OutputStream outputStream = mock(OutputStream.class);
        pinRenderNull.render("whatever", outputStream);
        verifyNoInteractions(outputStream);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeHeaders() {
        final Map<String, List<String>> responseHeaders = mock(Map.class);
        pinRenderNull.changeHeaders(responseHeaders);
        verifyNoInteractions(responseHeaders);
    }
}