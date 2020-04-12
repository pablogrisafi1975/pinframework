package com.pinframework;

import java.net.HttpURLConnection;

public class PinResponse {

    private final int status;
    private final Object obj;

    /**
     * @param status If you don't have nice constants around, use java.net.HttpURLConnection
     * @param obj    the object to be rendered
     */
    public PinResponse(int status, Object obj) {
        this.status = status;
        this.obj = obj;
    }

    public static PinResponse ok(Object obj) {
        return new PinResponse(HttpURLConnection.HTTP_OK, obj);
    }

    public static PinResponse notFound(Object obj) {
        return new PinResponse(HttpURLConnection.HTTP_NOT_FOUND, obj);
    }

    public static PinResponse badRequest(Object obj) {
        return new PinResponse(HttpURLConnection.HTTP_BAD_REQUEST, obj);
    }

    public static PinResponse internalError(Object obj) {
        return new PinResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, obj);
    }

    public int getStatus() {
        return status;
    }

    public Object getObj() {
        return obj;
    }

    public boolean keepResponseOpen() {
        return false;
    }

}
