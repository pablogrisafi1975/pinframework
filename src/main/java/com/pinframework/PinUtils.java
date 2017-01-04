package com.pinframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pinframework.exception.PinIORuntimeException;
import com.pinframework.exception.PinUnsupportedEncodingRuntimeException;

public class PinUtils {

	private static final Logger LOG = LoggerFactory.getLogger(PinUtils.class);

	private static final int COPY_BUFFER_SIZE = 8192;


	/**
	 * to fully read something an throw it away
	 */
	private static final OutputStream NULL_OUPUT_STREAM = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// just to clean things
		}
	};

	/**
	 * JSON that serializes LocalDateTime as yyyy-MM-ddTHH:mm:ssZ
	 */
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
				@Override
				public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
					return new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME));
				}
			}).create();

	private PinUtils() {
		// shup up sonar!
	}

	public static void fullyRead(InputStream in) throws IOException {
		copy(in, NULL_OUPUT_STREAM);
	}

	public static void copy(InputStream in, OutputStream out) {
		byte[] b = new byte[COPY_BUFFER_SIZE];
		int len;
		try {
			while ((len = in.read(b, 0, COPY_BUFFER_SIZE)) > 0) {
				out.write(b, 0, len);
			}
		} catch (IOException e) {
			throw new PinIORuntimeException(e);
		}
	}
	
	public static String asString(InputStream is) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PinUtils.copy(is, out);
		try {
			return out.toString(StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new PinUnsupportedEncodingRuntimeException(e);
		}
	}

	public static void put(Map<String, List<String>> map, String key, String value) {
		map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
	}

	/**
	 * Translates a string into {@code application/x-www-form-urlencoded} format
	 * using a specific encoding scheme. This method uses the UTF-8 encoding
	 * scheme to obtain the bytes for unsafe characters.
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World
	 * Wide Web Consortium Recommendation</a> states that UTF-8 should be used.
	 * Not doing so may introduce incompatibilities.</em>
	 *
	 * @param s
	 *            {@code String} to be translated.
	 * @return the translated {@code String}.
	 * @exception PinUnsupportedEncodingRuntimeException
	 *                If the named encoding is not supported. Should never
	 *                happen.
	 * @see URLDecoder#encode(java.lang.String, java.lang.String)
	 * @since 1.4
	 */
	public static String urlEncode(String string) {
		try {
			return URLEncoder.encode(string, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOG.error("Can not encode '{}'", string, e);
			throw new PinUnsupportedEncodingRuntimeException(e);
		}
	}

	/**
	 * Decodes a {@code application/x-www-form-urlencoded} string using a
	 * specific encoding scheme. UTF-8 encoding is used to determine what
	 * characters are represented by any consecutive sequences of the form
	 * "<i>{@code %xy}</i>".
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"> World
	 * Wide Web Consortium Recommendation</a> states that UTF-8 should be used.
	 * Not doing so may introduce incompatibilities.</em>
	 *
	 * @param s
	 *            the {@code String} to decode
	 * @return the newly decoded {@code String}
	 * @exception PinUnsupportedEncodingRuntimeException
	 *                If character encoding needs to be consulted, but named
	 *                character encoding is not supported. Should never happen.
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
