package com.pinframework.response;

import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.impl.PinRenderJson;

public class PinResponseOkJson extends PinResponse {

    private PinResponseOkJson(Object obj) {
        super(HttpURLConnection.HTTP_OK, obj, PinRenderJson.INSTANCE);
    }

    public static PinResponseOkJson of(Object obj) {
        return new PinResponseOkJson(obj);
    }

}
