package com.pinframework;

public final class PinRenderType {

    private PinRenderType() {
        //coverage
    }

    public static final String TEXT = "TEXT";
    public static final String JSON = "JSON";
    public static final String HTML = "HTML";
    public static final String DOWNLOAD = "DOWNLOAD";
    public static final String NULL = "NULL";
    /**
     * Expects a String and no header is added. It is up to you to include a header.<br>
     * You may use writeResponseContentType method, something like<br>
     * <code>ex.writeResponseContentType(PinContentType.TEXT_PLAIN_UTF8);</code>
     */
    public static final String PASSING = "PASSING";
}
