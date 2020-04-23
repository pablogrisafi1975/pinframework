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

import com.pinframework.UserDTO;
import com.pinframework.json.PinGsonBuilderFactory;

public class PinRenderJsonTest {

    private final PinRenderJson pinRenderJson = new PinRenderJson(PinGsonBuilderFactory.make().create());

    @Test
    public void getType() {
        assertEquals("JSON", pinRenderJson.getType());
    }

    @Test
    public void whenRenderSomethingThenRender() throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final UserDTO user = new UserDTO(333L, "John", "Smith");
        pinRenderJson.render(user, outputStream);
        String rendered = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals("{\"id\":333,\"firstName\":\"John\",\"lastName\":\"Smith\"}", rendered);
    }

    @Test
    public void whenRenderNullThenJustClose() throws IOException {
        final OutputStream outputStream = mock(OutputStream.class);
        pinRenderJson.render(null, outputStream);
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);
    }

    @Test
    public void changeHeaders() {
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        pinRenderJson.changeHeaders(responseHeaders);
        assertEquals("application/json; charset=utf-8", responseHeaders.get("Content-Type").get(0));
    }
}