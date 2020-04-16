package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

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
public class PinServerPostIT {

    private PinServer pinServer;

    private UserService userService;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeAll
    public void setup() {
        userService = new UserService();
        pinServer = new PinServerBuilder().build();
        pinServer.onPost("v1/users", ex -> {
            //post as application/json, parsed as a map<String, Object>
            //this is how services works nowadays
            //also supports multipart for uploading files
            Map<String, Object> postParams = ex.getPostParams();
            String firstName = (String) postParams.get("firstName");
            String lastName = (String) postParams.get("lastName");
            var user = new UserDTO(null, firstName, lastName);

            return PinResponse.ok(userService.savNew(user));

        });
        pinServer.onPost("v2/users", ex -> {
            //post as x-www-form-urlencoded, parsed as a map<String, List<String>>
            //this is how classic forms without files used to work
            //I need to cast as List<String> because this old format support arrays of elements
            Map<String, Object> postParams = ex.getPostParams();
            String firstName = ((List<String>) postParams.get("firstName")).get(0);
            String lastName = ((List<String>) postParams.get("lastName")).get(0);
            var user = new UserDTO(null, firstName, lastName);

            return PinResponse.ok(userService.savNew(user));

        });



        pinServer.start();
    }

    @BeforeEach
    public void reset() {
        userService.reset();
    }

    @Test
    public void postNewUserJsonOk() throws IOException {
        final RequestBody body = RequestBody
                .create("{\"firstName\":\"firstName100\",\"lastName\":\"lastName100\"}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName100\",\"lastName\":\"lastName100\"}", response.body().string());
        }
    }

    @Test
    public void postNewUserJsonBadRequest() throws IOException {
        final RequestBody body = RequestBody.create("{llalalala}", MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
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
    public void postNewUserFormUrlEncodedOk() throws IOException {
        final RequestBody body = RequestBody
                .create("firstName=firstName101&lastName=lastName101", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\"}", response.body().string());
        }
    }

    @Test
    public void postNewUserFormUrlEncodedBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users")
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

    //hay 4 posibilidades: json (lo manejo yo), x-www-form-urlencoded (lo manejo yo), form-data (via commons-upload) mixed (via commons upload)
    //todos pueden estar mal formados!!!w
    @Test
    public void postNewUserFormDataOk() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\"}", response.body().string());
        }
    }

    @Test
    public void postNewUserFormDataBadRequest() throws IOException {
        final RequestBody body = RequestBody
                .create("\\ // ñañañ \u1234 %x", MediaType.get("multipart/form-data"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
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
    public void postNewUserFormMixedOK() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("firstName", "firstName101")
                .addFormDataPart("lastName", "lastName101")
                .addPart(MultipartBody.Part.createFormData("file", "fileName", RequestBody.create(new byte[]{1,2,3})))
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":10,\"firstName\":\"firstName101\",\"lastName\":\"lastName101\"}", response.body().string());
        }
    }

    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }
}