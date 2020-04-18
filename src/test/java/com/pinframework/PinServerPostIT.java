package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerPostIT {

    private PinServer pinServer;

    private UserService userService;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeAll
    public void setup() {
        userService = new UserService();
        pinServer = new PinServerBuilder().build();
        pinServer.onPost("empty-ok", PinResponse.ok());
        pinServer.onPost("text-ok", PinResponse.ok("ok"), PinRenderType.TEXT);
        pinServer.onPost("echo/:text", ex -> PinResponse.ok(ex.getPathParam("text")), PinRenderType.TEXT);
        pinServer.onPost("always-error", ex -> {
            throw new IllegalArgumentException("my internal error");
        });
        pinServer.onPost("json/users", ex -> {
            //post as application/json, parsed as a map<String, Object>
            //only use this way if you are too lazy to write a class
            //remember even integers will be parsed as Doubles

            Map<String, Object> postParams = ex.getPostParams();
            String firstName = (String) postParams.get("firstName");
            String lastName = (String) postParams.get("lastName");
            List<String> tags = (List<String>) postParams.get("tags");

            var user = new UserDTO(null, firstName, lastName, tags);

            return PinResponse.ok(userService.savNew(user));
        });
        pinServer.onPost("json/v2/users", ex -> {
            //post a json as anything, Content-Type header will be ignored.
            //this is how services works normally
            var user = ex.getBodyAs(UserDTO.class);
            return PinResponse.ok(userService.savNew(user));
        });
        pinServer.onPost("json/v2/users-multi", ex -> {
            //post a json as anything, Content-Type header will be ignored.
            //this is how services works normally
            List<UserDTO> users = ex.getBodyAs(new TypeToken<List<UserDTO>>() {
            }.getType());

            return PinResponse.ok(users.stream().map(user -> userService.savNew(user)).collect(Collectors.toList()));
        });
        pinServer.onPost("form/users", ex -> {
            //post as x-www-form-urlencoded or as multipart, parsed as a map<String, List<String>>
            //this is how classic forms without files work
            //Also, this is how you upload files
            Map<String, List<String>> formParams = ex.getFormParams();
            String firstName = formParams.get("firstName").get(0);
            String lastName = formParams.get("lastName").get(0);
            List<String> tags = formParams.get("tags");

            List<FileItem> files = ex.getFileParams().get("file");
            FileItem fileItem1 = files != null && !files.isEmpty() ? files.get(0) : null;
            FileItem fileItem2 = files != null && !files.isEmpty() ? files.get(1) : null;
            UserDTO user;
            if (fileItem1 == null) {
                user = new UserDTO(null, firstName, lastName, tags);
            } else {
                user = new UserDTO(null, fileItem1.getName(),
                        new String(fileItem2.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }

            return PinResponse.ok(userService.savNew(user));

        });

        pinServer.start();
    }

    @BeforeEach
    public void reset() {
        userService.reset();
    }

    @Test
    public void postEmptyOk() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/empty-ok")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("", response.body().string());
        }
    }

    @Test
    public void postTextOk() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/text-ok")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("ok", response.body().string());
        }
    }

    @Test
    public void postEcho() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/echo/echo this")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("echo this", response.body().string());
        }
    }

    @Test
    public void postAlwaysErrorInternalError() throws IOException {
        final RequestBody body = RequestBody
                .create("this is not a valid json but Pin will not parse it", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/always-error")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"java.lang.IllegalArgumentException\",\"message\":\"my internal error\"}", response.body().string());
        }
    }

    @Test
    public void postNewUserJsonOk() throws IOException {
        final RequestBody body = RequestBody
                .create("{\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]}",
                        MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]}",
                    response.body().string());
        }
    }



    @Test
    public void postNewUserJsonBadRequest() throws IOException {
        final RequestBody body = RequestBody.create("{llalalala}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/users")
                .post(body)
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
    public void postNewUserJsonV2Ok() throws IOException {
        final RequestBody body = RequestBody
                .create("{\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]}",
                        MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/v2/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]}",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserJsonV2BadRequest() throws IOException {
        final RequestBody body = RequestBody.create("{llalalala}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/v2/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"com.google.gson.stream.MalformedJsonException: Expected ':' at line 1 column 12 path $.llalalala\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserJsonV2MultiOk() throws IOException {
        final RequestBody body = RequestBody
                .create("[{\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]}, {\"firstName\":\"firstName101\",\"lastName\":\"lastName101\",\"tags\":[\"c\",\"d\"]}]",
                        MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/json/v2/users-multi")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("[{\"id\":10,\"firstName\":\"firstName100\",\"lastName\":\"lastName100\",\"tags\":[\"a\",\"b\"]},{\"id\":11,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\",\"tags\":[\"c\",\"d\"]}]",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserFormUrlEncodedOk() throws IOException {
        final RequestBody body = RequestBody
                .create("firstName=firstName101&lastName=lastName101&tags=a&tags=b", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\",\"tags\":[\"a\",\"b\"]}",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserFormUrlEncodedBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .post(body)
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
    public void postNewUserFormDataOk() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .addFormDataPart("tags", "a")
                .addFormDataPart("tags", "c")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\",\"tags\":[\"a\",\"c\"]}",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserFormDataBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("multipart/form-data"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .post(body)
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
    public void postNewUserFormWithFileOK() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .addPart(MultipartBody.Part
                        .createFormData("file", "this is the file name 1", RequestBody.create("this is the file content 1".getBytes(
                                StandardCharsets.UTF_8))))
                .addPart(MultipartBody.Part
                        .createFormData("file", "this is the file name 2", RequestBody.create("this is the file content 2".getBytes(
                                StandardCharsets.UTF_8))))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/form/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"this is the file name 1\",\"lastName\":\"this is the file content 2\"}",
                    response.body().string());
        }
    }

    @Test
    public void postNewUserFormWithFileBadRequest() throws IOException {
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
                .url("http://localhost:9999/form/users")
                .post(body)
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