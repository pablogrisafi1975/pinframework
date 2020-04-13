package com.pinframework;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

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
        pinServer.onGet("v1/users/", PinResponse.ok(userService.list()));
        pinServer.onGet("v2/users/", ex -> {
            List<UserDTO> users = userService.list();
            var accept = ex.getRequestAccept();
            if(accept.contains("text/html")){
                //in real world you must use a template library like freemaker
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body><table><th><td>id</td><td>First Name</td><td>Last Name</td></th>");
                for (UserDTO user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getId() + "</td>");
                    sb.append("<td>" + user.getFirstName() + "</td>");
                    sb.append("<td>" + user.getLastName() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table></body></html>");

                ex.writeResponseContentType(PinContentType.TEXT_HTML);

                return PinResponse.ok(sb.toString());
            }
            if(accept.contains("text/plain")){
                //in real world you must use a csv library like commons csv
                StringBuilder sb = new StringBuilder();
                sb.append("Id;First Name;Last Name\n");
                for (UserDTO user : users) {
                    sb.append(user.getId() + ";");
                    sb.append(user.getFirstName() + ";");
                    sb.append(user.getLastName() + "\n");
                }

                ex.writeResponseContentType(PinContentType.TEXT_PLAIN_UTF8);

                return PinResponse.ok(sb.toString());
            }
            return PinResponse.ok(users);
        }, PinRenderType.PASSING);


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
        //this is a shorter version of user service. Is less code, but you can't return a message when not found
        pinServer.onGet("v2/users/:id", ex -> {
            Long id;
            try {
                id = Long.parseLong(ex.getPathParams().get("id"));
            } catch (Exception e) {
                return PinResponse.badRequest(e);
            }

            return PinResponse.from(userService.get(id));

        });
        //this is an even shorter version of user service. Is less code, but you can't return a message when not found and
        //you return internal error even in bad requests
        pinServer.onGet("v3/users/:id", ex -> {
            Long id = Long.parseLong(ex.getPathParams().get("id"));
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
            assertEquals(response.code(), 200);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.TEXT_PLAIN_UTF8);
            assertEquals(response.body().string(), "this is the text");
        }
    }

    @Test
    public void getConstantText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/constant-text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.TEXT_PLAIN_UTF8);
            assertEquals(response.body().string(), "this is the constant text");
        }
    }

    @Test
    public void getV1UsersAsJson() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            String body = response.body().string();
            assertTrue(body.startsWith("[{\"id\":0,\"firstName\":\"firstName0\",\"lastName\":\"lastName0\"},"));
            assertTrue(body.endsWith(",{\"id\":9,\"firstName\":\"firstName9\",\"lastName\":\"lastName9\"}]"));
        }
    }

    @Test
    public void getV1Users() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            String body = response.body().string();
            assertTrue(body.startsWith("[{\"id\":0,\"firstName\":\"firstName0\",\"lastName\":\"lastName0\"},"));
            assertTrue(body.endsWith(",{\"id\":9,\"firstName\":\"firstName9\",\"lastName\":\"lastName9\"}]"));
        }
    }
    @Test
    public void getV2UsersAsCSV() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users")
                .addHeader("Accept", "text/plain")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.TEXT_PLAIN_UTF8);
            String body = response.body().string();
            assertTrue(body.startsWith("Id;First Name;Last Name\n"));
            assertTrue(body.endsWith("\n9;firstName9;lastName9\n"));
        }
    }

    @Test
    public void getV1UsersWithSlash() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            String body = response.body().string();
            assertTrue(body.startsWith("[{\"id\":0,\"firstName\":\"firstName0\",\"lastName\":\"lastName0\"},"));
            assertTrue(body.endsWith(",{\"id\":9,\"firstName\":\"firstName9\",\"lastName\":\"lastName9\"}]"));
        }
    }

    @Test
    public void getV1UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getV1UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "{\"type\":\"NOT_FOUND\",\"message\":\"There is no user with id = 300\"}");
        }
    }

    @Test
    public void getV1UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_BAD_REQUEST);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getV1UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @Test
    public void getV2UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getV2UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "");
        }
    }

    @Test
    public void getV2UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_BAD_REQUEST);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getV2UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @Test
    public void getV3UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), 200);
            assertEquals(response.header("Content-Type"), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}");
        }
    }

    @Test
    public void getV3UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(), "");
        }
    }

    @Test
    public void getV3ShouldBeUserBadRequestButInternalError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}");
        }
    }

    @Test
    public void getV3UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_INTERNAL_ERROR);
            assertEquals(response.header(PinContentType.CONTENT_TYPE), PinContentType.APPLICATION_JSON_UTF8);
            assertEquals(response.body().string(),
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}");
        }
    }

    @AfterClass
    public void tearDown() {
        pinServer.stop(1);
    }
}