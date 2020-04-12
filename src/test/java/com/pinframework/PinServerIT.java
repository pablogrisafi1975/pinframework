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
        pinServer.onGet("constant-text", PinResponse.ok("this is the constant text"), PinRenderType.TEXT);
        pinServer.onGet("text", ex -> PinResponse.ok("this is the text"), PinRenderType.TEXT);
        pinServer.onGet("v1/users/:id", ex -> {
            Long id;
            try {
                id = Long.parseLong(ex.getPathParams().get("id"));
            } catch (Exception e) {
                return PinResponse.badRequest(e);
            }

            UserDTO userDTO = userService.get(id);
            if (userDTO != null) {
                return PinResponse.ok(userDTO);
            } else {
                return PinResponse.notFound(new MessageDTO("NOT_FOUND", "There is no user with id = " + id));
            }
        });
        //this is a shorter version of user service. Is less code, but you can't return a message
        pinServer.onGet("v2/users/:id", ex -> {
            Long id;
            try {
                id = Long.parseLong(ex.getPathParams().get("id"));
            } catch (Exception e) {
                return PinResponse.badRequest(e);
            }

            return PinResponse.from(userService.get(id));

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
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.TEXT_PLAIN_UTF8);
            Assert.assertEquals(response.body().string(), "this is the text");
        }
    }

    @Test
    public void getConstantText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/constant-text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.TEXT_PLAIN_UTF8);
            Assert.assertEquals(response.body().string(), "this is the constant text");
        }
    }

    @Test
    public void getV1UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getV1UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(), "{\"type\":\"NOT_FOUND\",\"message\":\"There is no user with id = 300\"}");
        }
    }

    @Test
    public void getV1UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_BAD_REQUEST);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getV1UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @Test
    public void getV2UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), 200);
            Assert.assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getV2UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(), "");
        }
    }

    @Test
    public void getV2UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_BAD_REQUEST);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getV2UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            Assert.assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            Assert.assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @AfterClass
    public void tearDown() {
        pinServer.stop(1);
    }
}