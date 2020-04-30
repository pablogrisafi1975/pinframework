package com.pinframework;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PinUtils {

    private static final int COPY_BUFFER_SIZE = 8192;

    private PinUtils() {
        // shup up sonar!
    }

    /**
     * to fully read something an throw it away
     */
    private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) {
            // just to clean things
        }
    };

    public static void fullyRead(InputStream in) throws IOException {
        copy(in, NULL_OUTPUT_STREAM);
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
    public static String removeTrailingSlash(String string) {
        if (string == null) {
            return null;
        }
        if (string.length() == 0) {
            return string;
        }
        if (string.charAt(string.length() - 1) == '/') {
            return string.substring(0, string.length() - 1);
        }
        return string;
    }

    public static Map<String, String> splitPath(String requestPath, String contextPath, List<String> pathParamNames) {
        String[] pathParamValues = requestPath.replaceFirst(Pattern.quote(contextPath), "").split("/", -1);
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

    public static String maximalPathValidAsContext(String fullPath) {
        int indexOfColon = fullPath.indexOf(':');
        if(indexOfColon == -1){
            return fullPath;
        }
        return fullPath.substring(0, indexOfColon - 1); //-1 to remove the last /
    }
}
