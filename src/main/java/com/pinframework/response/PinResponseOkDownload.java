package com.pinframework.response;

import java.io.InputStream;
import java.net.HttpURLConnection;

import com.pinframework.PinResponse;
import com.pinframework.impl.PinRenderFileDownload;

public class PinResponseOkDownload extends PinResponse {

    private PinResponseOkDownload(InputStream inputStream, String fileName) {
        super(HttpURLConnection.HTTP_OK, inputStream, new PinRenderFileDownload(fileName));
    }

    public static PinResponseOkDownload of(InputStream inputStream, String fileName) {
        return new PinResponseOkDownload(inputStream, fileName);
    }

}
