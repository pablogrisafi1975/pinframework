package com.pinframework;

import java.net.HttpURLConnection;
import java.util.Optional;

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

    public static PinResponse ok() {
        return new PinResponse(HttpURLConnection.HTTP_OK, null);
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

    /**
     * Creates the proper response and status for a object
     *
     * @param obj the object
     * @return if the object is null or an empty optional, status is 404=NOT_FOUND and nothing is rendered<br>
     * if the object is non empty optional, status is 200=OK and the wrapped object is rendered<br>
     * if the object is non null, status is 200=OK and the object is rendered
     */
    public static PinResponse from(Object obj) {
        if (obj == null) {
            return new PinResponse(HttpURLConnection.HTTP_NOT_FOUND, null);
        }
        if (obj instanceof Optional) {
            var opt = (Optional<?>) obj;
            if (opt.isEmpty()) {
                return new PinResponse(HttpURLConnection.HTTP_NOT_FOUND, null);
            } else {
                return new PinResponse(HttpURLConnection.HTTP_OK, opt.get());
            }
        }

        return new PinResponse(HttpURLConnection.HTTP_OK, obj);

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
