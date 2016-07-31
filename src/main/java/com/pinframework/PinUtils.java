package com.pinframework;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pinframework.exceptions.PinUnsupportedEncodingRuntimeException;

public class PinUtils {

	private static final Logger LOG = LoggerFactory.getLogger(PinUtils.class);

	private static final int COPY_BUFFER_SIZE = 8192;

	/**
	 * value = UTF-8 . It is used in lots of places
	 */
	public static final String UTF_8 = "UTF-8";

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

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[COPY_BUFFER_SIZE];
		int len;
		while ((len = in.read(b, 0, COPY_BUFFER_SIZE)) > 0) {
			out.write(b, 0, len);
		}
	}

	public static Map<String, List<String>> splitQuery(String decodedQuery) {
		if (decodedQuery == null || decodedQuery.trim().length() == 0) {
			return Collections.emptyMap();
		}
		return Arrays.stream(decodedQuery.split("&")).map(PinUtils::splitQueryParameter)
				.collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new,
						Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
	}

	public static SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
		final int idx = it.indexOf('=');
		final String key = idx > 0 ? it.substring(0, idx) : it;
		final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
		return new SimpleImmutableEntry<>(key, value);
	}

	/**
	 * appContext should look like <br>
	 * admin<br>
	 * admin/user<br>
	 * admin/user/:userId<br>
	 * admin/office/:officeId/:userId<br>
	 * so <br>
	 * dont start or finish with / <br>
	 * path parameters should be :name <br>
	 * once you put a path parameter, everything else should be a path
	 * parameters<br>
	 * so this admin/office/:officeId/:userId is valid , but<br>
	 * this admin/office/:officeId/users/:userId is invalid<br>
	 * and this admin/office/help:officeId/users/:userId is invalid<br>
	 */
	public static List<String> contextAndPathParameters(String appContext, String path) {
		int firstColonIndex = path.indexOf(':');
		if (firstColonIndex == -1) {
			return Collections.singletonList(appContext + path);
		}
		String fullContext = appContext + path.substring(0, firstColonIndex - 1); // -1
																					// to
																					// remove
																					// finishing
																					// /
		String fullPaths = path.substring(firstColonIndex);
		List<String> pathParameters = Arrays.stream(fullPaths.split("/", -1)).map(s -> s.substring(1))
				.collect(Collectors.toList());
		List<String> result = new ArrayList<>();
		result.add(fullContext);
		result.addAll(pathParameters);
		return result;
	}

	public static Map<String, String> splitPath(String requestPath, String contextPath, List<String> pathParamNames) {
		String[] pathParamValues = requestPath.replace(contextPath, "").split("/", -1);
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			// pathParamValues has a starting /, so string.split creates a first
			// null value we need to skip
			// thats why i + 1
			String value = i < pathParamValues.length - 1 ? pathParamValues[i + 1] : null;
			map.put(key, value);
		}
		return map;
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
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilities.</em>
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
			return URLEncoder.encode(string, UTF_8);
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
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilities.</em>
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
			return URLDecoder.decode(string, UTF_8);
		} catch (UnsupportedEncodingException e) {
			LOG.error("Can not decode '{}'", string, e);
			throw new PinUnsupportedEncodingRuntimeException(e);
		}
	}

}
