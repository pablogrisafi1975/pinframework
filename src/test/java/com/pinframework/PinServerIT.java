package com.pinframework;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.pinframework.response.PinResponseOkText;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PinServerIT {

    private PinServer pinServer;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeClass
    public void setup() {
        pinServer = new PinServerBuilder().build();
        pinServer.onGet("text", ex -> PinResponseOkText.of("this is the text"));
        pinServer.start();
    }

    @Test
    public void getText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.body().string(), "this is the text");
            Assert.assertEquals(response.header("Content-Type"), "text/plain; charset=utf-8");
        }
    }

    @AfterClass
    public void tearDown() {
        pinServer.stop(10);
    }
}