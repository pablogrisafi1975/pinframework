package com.pinframework.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PinRenderFileDownloadTest {
    PinRenderFileDownload pinRenderFileDownload = new PinRenderFileDownload();

    @Test
    public void getType() {
        assertEquals("DOWNLOAD", pinRenderFileDownload.getType());
    }

    @Test
    public void whenRenderAnythingThenJustClose() throws IOException {
        final OutputStream outputStream = mock(OutputStream.class);
        pinRenderFileDownload.render(null, outputStream);
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeHeaders() {
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        pinRenderFileDownload.changeHeaders(responseHeaders);
        assertEquals("application/force-download", responseHeaders.get("Content-Type").get(0));
    }
}