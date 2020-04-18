package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerPutIT {

    private PinServer pinServer;

    private UserService userService;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeAll
    public void setup() {
        userService = new UserService();
        pinServer = new PinServerBuilder().build();
        pinServer.onPut("empty-ok", PinResponse.ok());
        pinServer.onPut("text-ok", PinResponse.ok("ok"), PinRenderType.TEXT);
        pinServer.onPut("echo/:text", ex -> PinResponse.ok(ex.getPathParam("text")), PinRenderType.TEXT);
        pinServer.onPut("always-error", ex -> {
            throw new IllegalArgumentException("my internal error");
        });
        pinServer.onPut("json/users", ex -> {
            //post as application/json, parsed as a map<String, Object>
            Map<String, Object> postParams = ex.getPostParams();
            Long id = ((Double) postParams.get("id")).longValue();
            String firstName = (String) postParams.get("firstName");
            String lastName = (String) postParams.get("lastName");
            var user = new UserDTO(id, firstName, lastName);

            return PinResponse.ok(userService.update(user));

        });
        pinServer.onPut("form/users", ex -> {
            //post as x-www-form-urlencoded or multipart, parsed as a map<String, List<String>>
            //this is how classic forms without files used to work
            Map<String, List<String>> formParams = ex.getFormParams();
            Long id = Long.parseLong(formParams.get("id").get(0));
            String firstName = formParams.get("firstName").get(0);
            String lastName = formParams.get("lastName").get(0);
            List<FileItem> files = ex.getFileParams().get("file");
            FileItem fileItem = files != null && !files.isEmpty() ? files.get(0) : null;
            UserDTO user;
            if (fileItem == null) {
                user = new UserDTO(id, firstName, lastName);
            } else {
                user = new UserDTO(id, fileItem.getName(), new String(fileItem.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }
            return PinResponse.ok(userService.update(user));


        });

        pinServer.start();
    }

    @BeforeEach
    public void reset() {
        userService.reset();
    }

    @Test
    public void putEmptyOk() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/empty-ok")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("", response.body().string());
        }
    }

    @Test
    public void putTextOk() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/text-ok")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("ok", response.body().string());
        }
    }

    @Test
    public void putEcho() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/echo/echo this")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("echo this", response.body().string());
        }
    }

    @Test
    public void putAlwaysErrorInternalError() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/always-error")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"java.lang.IllegalArgumentException\",\"message\":\"my internal error\"}", response.body().string());
        }
    }

    @Test
    public void putNewUserJsonOk() throws IOException {
        final RequestBody body = RequestBody
                .create("{\"id\":5, \"firstName\":\"firstName100\",\"lastName\":\"lastName100\"}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":5,\"firstName\":\"firstName100\",\"lastName\":\"lastName100\"}", response.body().string());
        }
    }

    @Test
    public void putNewUserJsonBadRequest() throws IOException {
        final RequestBody body = RequestBody.create("{llalalala}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"com.google.gson.stream.MalformedJsonException: Expected ':' at line 1 column 12 path $.\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @Test
    public void putNewUserFormUrlEncodedOk() throws IOException {
        final RequestBody body = RequestBody
                .create("id=5&firstName=firstName101&lastName=lastName101", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":5,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\"}", response.body().string());
        }
    }

    @Test
    public void putNewUserFormUrlEncodedBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"URLDecoder: Incomplete trailing escape (%) pattern\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @Test
    public void putNewUserFormDataOk() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", "5")
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":5,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\"}", response.body().string());
        }
    }

    @Test
    public void putNewUserFormDataBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("multipart/form-data"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"the request was rejected because no multipart boundary was found\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @Test
    public void putNewUserFormWithFileOK() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", "5")
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .addPart(MultipartBody.Part
                        .createFormData("file", "this is the file name", RequestBody.create("this is the file content".getBytes(
                                StandardCharsets.UTF_8))))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":5,\"firstName\":\"this is the file name\",\"lastName\":\"this is the file content\"}",
                    response.body().string());
        }
    }

    @Test
    public void putNewUserFormWithFileBadRequest() throws IOException {
        final String badMultipart = "--4e0eb713-ebef-4747-954b-d087f607cf00\n"
                + "Content-Disposition: form-data; name=\"firstName\"\n"
                + "Content-Length: 12\n"
                + "\n"
                + "firstName101\n"
                + "--4e0eb713-ebef-4747-954b-d087f607cf00\n"
                + "Content-Disposition: form-data; name=\"lastName\"\n"
                + "Content-Length: 11\n"
                + "\n"
                + "lastName101\n"
                + "--4e0eb713-ebef-4747-954b-d087f607cf00\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"this is the file name\n" //<-- missing a closing quote here
                + "Content-Length: 24\n"
                + "\n"
                + "this is the file content\n"
                + "--4e0eb713-ebef-4747-954b-d087f607cf00--\n";
        final RequestBody body = RequestBody.create(badMultipart, MediaType.parse("multipart/form-data"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"the request was rejected because no multipart boundary was found\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }
}