package com.pinframework.render;

import static org.testng.Assert.assertEquals;

import com.pinframework.constant.PinContentType;
import com.pinframework.constant.PinHeader;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

@Test
public class PinRenderTextUtf8Test {

  private final PinRenderTextUtf8 pinRenderTextUtf8 = new PinRenderTextUtf8();

  @Test
  public void render_string() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    pinRenderTextUtf8.render("this_is_a_string", outputStream);
    assertEquals(outputStream.toByteArray(), "this_is_a_string".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void render_list() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    pinRenderTextUtf8.render(Arrays.asList("a", "b", "c"), outputStream);
    assertEquals(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), "[a, b, c]");
  }

  @Test
  public void render_null() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    pinRenderTextUtf8.render(null, outputStream);
    assertEquals(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), "null");
  }

  @Test
  public void changeHeaders() throws Exception {
    Map<String, List<String>> map = new HashMap<>();
    pinRenderTextUtf8.changeHeaders(map);
    assertEquals(map.get(PinHeader.CONTENT_TYPE).get(0), PinContentType.TEXT_PLAIN_UTF8);
  }

}
