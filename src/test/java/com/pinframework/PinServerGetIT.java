package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerGetIT {

    private PinServer pinServer;

    private UserService userService;

    private final OkHttpClient client = new OkHttpClient();

    @BeforeAll
    public void setup() {
        userService = new UserService();
        pinServer = new PinServerBuilder().build();
        pinServer.onGet("constant-text", PinResponse.ok("this is the constant text"), PinRenderType.TEXT);
        pinServer.onGet("text", ex -> PinResponse.ok("this is the text"), PinRenderType.TEXT);
        pinServer.onGet("v1/users/", PinResponse.ok(userService.list()));
        pinServer.onGet("v2/users/", ex -> {
            String expectedFirstName = ex.getQueryParamFirst("firstName");
            String expectedLastName = ex.getQueryParamFirst("lastName");
            List<UserDTO> users = userService.list(expectedFirstName, expectedLastName);
            return PinResponse.ok(users);
        });

        pinServer.onGet("v2/users-html/", ex -> {
            String expectedFirstName = ex.getQueryParamFirst("firstName");
            String expectedLastName = ex.getQueryParamFirst("lastName");
            List<UserDTO> users = userService.list(expectedFirstName, expectedLastName);
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
            return PinResponse.ok(sb.toString());
        }, PinRenderType.HTML);

        pinServer.onGet("v2/users-text/", ex -> {
            String expectedFirstName = ex.getQueryParamFirst("firstName");
            String expectedLastName = ex.getQueryParamFirst("lastName");
            List<UserDTO> users = userService.list(expectedFirstName, expectedLastName);
            //in real world you must use a csv library like commons csv
            StringBuilder sb = new StringBuilder();
            sb.append("Id;First Name;Last Name\n");
            for (UserDTO user : users) {
                sb.append(user.getId() + ";");
                sb.append(user.getFirstName() + ";");
                sb.append(user.getLastName() + "\n");
            }

            return PinResponse.ok(sb.toString());
        }, PinRenderType.TEXT);

        pinServer.onGet("v2/users-download/", ex -> {
            String expectedFirstName = ex.getQueryParamFirst("firstName");
            String expectedLastName = ex.getQueryParamFirst("lastName");
            List<UserDTO> users = userService.list(expectedFirstName, expectedLastName);

            //write the file name first!
            ex.writeDownloadFileName("all-users.csv");

            //in real world you must use a csv library like commons csv
            //note that you write directly to the output stream, so no memory is used
            ex.writeResponseContentLine("Id;First Name;Last Name");
            for (UserDTO user : users) {
                ex.writeResponseContent(user.getId() + ";");
                ex.writeResponseContent(user.getFirstName() + ";");
                ex.writeResponseContentLine(user.getLastName());
            }


            return PinResponse.download();

        }, PinRenderType.DOWNLOAD);

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
                id = Long.parseLong(ex.getPathParam("id"));
            } catch (Exception e) {
                return PinResponse.badRequest(e);
            }

            return PinResponse.okOrNotFound(userService.get(id));

        });
        //this is an even shorter version of user service. Is less code, but you can't return a message when not found and
        //you have a fixed message on bad request
        pinServer.onGet("v3/users/:id", ex -> {
            Long id = ex.getPathParamAsLong("id");
            return PinResponse.okOrNotFound(userService.get(id));

        });
        pinServer.start();
    }

    @Test
    public void getText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("this is the text", response.body().string());
        }
    }

    @Test
    public void getConstantText() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/constant-text")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("this is the constant text", response.body().string());
        }
    }

    @Test
    public void getV1UsersAsJson() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
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
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            String body = response.body().string();
            assertTrue(body.startsWith("[{\"id\":0,\"firstName\":\"firstName0\",\"lastName\":\"lastName0\"},"));
            assertTrue(body.endsWith(",{\"id\":9,\"firstName\":\"firstName9\",\"lastName\":\"lastName9\"}]"));
        }
    }

    @Test
    public void getV2UsersAsCSV() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users-text")
                .addHeader("Accept", "text/plain")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            String body = response.body().string();
            assertTrue(body.startsWith("Id;First Name;Last Name\n"));
            assertTrue(body.endsWith("\n9;firstName9;lastName9\n"));
        }
    }

    @Test
    public void getV2UsersAsCSVFiltered() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users-text?firstName=3")
                .addHeader("Accept", "text/plain")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_PLAIN_UTF8, response.header(PinContentType.CONTENT_TYPE));
            String body = response.body().string();
            assertEquals("Id;First Name;Last Name\n3;firstName3;lastName3\n", body);
        }
    }

    @Test
    public void getV2UsersAsJsonFiltered() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users?lastName=5")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            String body = response.body().string();
            assertEquals("[{\"id\":5,\"firstName\":\"firstName5\",\"lastName\":\"lastName5\"}]", body);
        }
    }

    @Test
    public void getV2UsersAsHTML() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users-html")
                .addHeader("Accept", "text/html")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.TEXT_HTML_UTF8, response.header(PinContentType.CONTENT_TYPE));
            String body = response.body().string();
            assertTrue(body.startsWith("<html>"));
            assertTrue(body.endsWith("</html>"));
        }
    }

    @Test
    public void getV2UsersAsFile() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users-download")
                .addHeader("Accept", "application/octet-stream")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_FORCE_DOWNLOAD, response.header(PinContentType.CONTENT_TYPE));
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
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
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
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}", response.body().string());
        }
    }

    @Test
    public void getV1UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"NOT_FOUND\",\"message\":\"There is no user with id = 300\"}", response.body().string());
        }
    }

    @Test
    public void getV1UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}",
                    response.body().string());
        }
    }

    @Test
    public void getV1UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v1/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}", response.body().string());
        }
    }

    @Test
    public void getV2UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}", response.body().string());
        }
    }

    @Test
    public void getV2UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("", response.body().string());
        }
    }

    @Test
    public void getV2UserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"java.lang.NumberFormatException\",\"message\":\"For input string: \\\"xxx\\\"\"}",
                    response.body().string());
        }
    }

    @Test
    public void getV2UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v2/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}", response.body().string());
        }
    }

    @Test
    public void getV3UserFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/3")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"id\":3,\"firstName\":\"firstName3\",\"lastName\":\"lastName3\"}", response.body().string());
        }
    }

    @Test
    public void getV3UserNotFound() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/300")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(response.code(), HttpURLConnection.HTTP_NOT_FOUND);
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("", response.body().string());
        }
    }

    @Test
    public void getV3ShouldBeUserBadRequest() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/xxx")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals(
                    "{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field id with value xxx can not be converted to Long\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"id\",\"currentValue\":\"xxx\",\"destinationClassName\":\"Long\"}",
                    response.body().string());
        }
    }

    @Test
    public void getV3UserInternalServerError() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/v3/users/-2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}",
                    response.body().string());
        }
    }

    public void getCheckQueryParamSimple() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/query-params-simple?longValue=333$dayOfWeek=MONDAY&date=2020-01-02&dateTime=2020-01-02T03:04:05&zonedDateTime=2020-01-02T03:04:05-04:00")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"java.lang.NullPointerException\",\"message\":\"Fake internal error\"}",
                    response.body().string());
        }
    }

    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }
}