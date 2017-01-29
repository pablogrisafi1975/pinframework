package com.pinframework;

import com.pinframework.exception.PinIoRuntimeException;
import com.pinframework.exception.PinUnsupportedEncodingRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PinUtils {

  private static final Logger LOG = LoggerFactory.getLogger(PinUtils.class);

  private static final int COPY_BUFFER_SIZE = 8192;
  /**
   * to fully read something an throw it away.
   */
  private static final OutputStream NULL_OUPUT_STREAM = new OutputStream() {
    @Override
    public void write(int buffer) throws IOException {
      // just to clean things
    }
  };

  private PinUtils() {
    // shup up sonar!
  }

  public static void fullyRead(InputStream in) throws IOException {
    copy(in, NULL_OUPUT_STREAM);
  }

  public static void copy(InputStream in, OutputStream out) {
    byte[] byteBuffer = new byte[COPY_BUFFER_SIZE];
    int len;
    try {
      while ((len = in.read(byteBuffer, 0, COPY_BUFFER_SIZE)) > 0) {
        out.write(byteBuffer, 0, len);
      }
    } catch (IOException ex) {
      throw new PinIoRuntimeException(ex);
    }
  }

  public static String asString(InputStream is) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PinUtils.copy(is, out);
    try {
      return out.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException ex) {
      throw new PinUnsupportedEncodingRuntimeException(ex);
    }
  }

  public static void put(Map<String, List<String>> map, String key, String value) {
    map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
  }

  /**
   * Translates a string into {@code application/x-www-form-urlencoded} format using a specific
   * encoding scheme. This method uses the UTF-8 encoding scheme to obtain the bytes for unsafe
   * characters.
   * <p>
   * <em><strong>Note:</strong> The
   * <a href= "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World Wide Web
   * Consortium Recommendation</a> states that UTF-8 should be used. Not doing so may introduce
   * incompatibilities.</em>
   *
   * @param s {@code String} to be translated.
   * @return the translated {@code String}.
   * @exception PinUnsupportedEncodingRuntimeException If the named encoding is not supported.
   *            Should never happen.
   * @see URLDecoder#encode(java.lang.String, java.lang.String)
   * @since 1.4
   */
  public static String urlEncode(String string) {
    try {
      return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException ex) {
      LOG.error("Can not encode '{}'", string, ex);
      throw new PinUnsupportedEncodingRuntimeException(ex);
    }
  }

  /**
   * Decodes a {@code application/x-www-form-urlencoded} string using a specific encoding scheme.
   * UTF-8 encoding is used to determine what characters are represented by any consecutive
   * sequences of the form "<i>{@code %xy}</i>".
   * 
   * <p>
   * <em><strong>Note:</strong> The
   * <a href= "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World Wide Web
   * Consortium Recommendation</a> states that UTF-8 should be used. Not doing so may introduce
   * incompatibilities.</em>
   *
   * @param s the {@code String} to decode
   * @return the newly decoded {@code String}
   * @exception PinUnsupportedEncodingRuntimeException If character encoding needs to be consulted,
   *            but named character encoding is not supported. Should never happen.
   * @see URLEncoder#decode(java.lang.String, java.lang.String)
   */
  public static String urlDecode(String string) {
    try {
      return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      LOG.error("Can not decode '{}'", string, e);
      throw new PinUnsupportedEncodingRuntimeException(e);
    }
  }

  public static String getFirst(Map<String, List<String>> map, String key) {
    if (map.containsKey(key)) {
      List<String> list = map.get(key);
      if (list.isEmpty()) {
        return null;
      }
      return list.get(0);
    }
    return null;
  }



}
