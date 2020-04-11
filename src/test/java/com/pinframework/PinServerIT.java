package com.pinframework;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PinServerIT {

    private PinServer pinServer;

    private UserService userService;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeClass
    public void setup() {
        userService = new UserService();
        pinServer = new PinServerBuilder().build();
        pinServer.onGet("constant-text", PinResponse.okText("this is the constant text"));
        pinServer.onGet("text", ex -> PinResponse.okText("this is the text"));
        pinServer.onGet("users/:id", ex -> {
            Long id;
            try {
                id = Long.parseLong(ex.getPathParams().get("id"));
            } catch (Exception e) {
                return new PinResponse(HttpURLConnection.HTTP_BAD_REQUEST, e, pinServer.findRender(ex.getRequestContentTypeParsed()));
            }

            UserDTO userDTO = userService.get(id);
            if (userDTO != null) {
                return PinResponse.okJson(userDTO);
            } else {
                return PinResponse.notFoundJson(null);
            }
        });
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

    @Test
    public void getConstantText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/constant-text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.header("Content-Type"), "text/plain; charset=utf-8");
            Assert.assertEquals(response.body().string(), "this is the constant text");
        }
    }

    @Test
    public void getUserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8");
            Assert.assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getUserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8");
            Assert.assertEquals(response.body().string(), "null");
        }
    }

    @Test
    public void getUserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_BAD_REQUEST);
            Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8");
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getUserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            Assert.assertEquals(response.header("Content-Type"), "application/json; charset=utf-8");
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @AfterClass
    public void tearDown() {
        pinServer.stop(1);
    }
}