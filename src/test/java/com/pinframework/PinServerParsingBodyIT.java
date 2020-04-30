package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.gson.Gson;
import com.pinframework.json.PinGsonBuilderFactory;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerParsingBodyIT {

    private PinServer pinServer;

    private final OkHttpClient client = new OkHttpClient();

    private final Gson gson = PinGsonBuilderFactory.make().create();

    @BeforeAll
    public void setup() {
        pinServer = new PinServerBuilder().build();

        pinServer.onPost("form-params-simple", ex -> {
            Long longValue = ex.getFormParamFirstAsLong("longValue");
            DayOfWeek dayOfWeek = ex.getFormParamFirstAsEnum("dayOfWeek", DayOfWeek.class);
            LocalDate localDate = ex.getFormParamFirstAsLocalDate("localDate");
            LocalDateTime localDateTime = ex.getFormParamFirstAsLocalDateTime("localDateTime");
            ZonedDateTime zonedDateTime = ex.getFormParamFirstAsZonedDateTime("zonedDateTime");

            var obj = new SpecialParserDTO();
            obj.setLongValue(longValue);
            obj.setDayOfWeek(dayOfWeek);
            obj.setLocalDate(localDate);
            obj.setLocalDateTime(localDateTime);
            obj.setZonedDateTime(zonedDateTime);

            return PinResponse.ok(obj);

        });

        pinServer.onPost("form-params-list", ex -> {
            List<Long> longList = ex.getFormParamAsLongList("longList");
            List<DayOfWeek> dayOfWeekList = ex.getFormParamAsEnumList("dayOfWeekList", DayOfWeek.class);
            List<LocalDate> localDateList = ex.getFormParamAsLocalDateList("localDateList");
            List<LocalDateTime> localDateTimeList = ex.getFormParamAsLocalDateTimeList("localDateTimeList");
            List<ZonedDateTime> zonedDateTimeList = ex.getFormParamAsZonedDateTimeList("zonedDateTimeList");

            var obj = new SpecialParserListDTO();
            obj.setLongList(longList);
            obj.setDayOfWeekList(dayOfWeekList);
            obj.setLocalDateList(localDateList);
            obj.setLocalDateTimeList(localDateTimeList);
            obj.setZonedDateTimeList(zonedDateTimeList);

            return PinResponse.ok(obj);

        });

        pinServer.onPost("body-params", ex -> {

            var obj = ex.getBodyAs(SpecialParserDTO.class);

            return PinResponse.ok(obj);

        });

        pinServer.start();
    }

    @Test
    public void postFormParamSimple() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDate", "2020-01-02")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserDTO obj = gson.fromJson(response.body().string(), SpecialParserDTO.class);
            assertEquals(333L, obj.getLongValue());
            assertEquals(DayOfWeek.MONDAY, obj.getDayOfWeek());
            assertEquals(LocalDate.of(2020, 1, 2), obj.getLocalDate());
            assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), obj.getLocalDateTime());
            assertEquals(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)), obj.getZonedDateTime());
        }
    }

    @Test
    public void postFormParamSimpleWrongEnum() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONccccccDAY")
                .addFormDataPart("localDate", "2020-01-02")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field dayOfWeek with value MONccccccDAY can not be converted to DayOfWeek[MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY]\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"dayOfWeek\",\"currentValue\":\"MONccccccDAY\",\"destinationClassName\":\"DayOfWeek\"}",
                    response.body().string());
        }
    }

    @Test
    public void postFormParamSimpleWrongLocalDate() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDate", "2020-0x1-02")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field localDate with value 2020-0x1-02 can not be converted to LocalDate\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"localDate\",\"currentValue\":\"2020-0x1-02\",\"destinationClassName\":\"LocalDate\"}",
                    response.body().string());
        }
    }

    @Test
    public void postFormParamSimpleWrongLocalDateTime() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDate", "2020-01-02")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05Z")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field localDateTime with value 2020-01-02T03:04:05Z can not be converted to LocalDateTime\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"localDateTime\",\"currentValue\":\"2020-01-02T03:04:05Z\",\"destinationClassName\":\"LocalDateTime\"}",
                    response.body().string());
        }
    }

    @Test
    public void postFormParamSimpleWrongZonedDateTime() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDate", "2020-01-02")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"The field zonedDateTime with value 2020-01-02T03:04:05 can not be converted to ZonedDateTime\",\"messageKey\":\"CAN_NOT_CONVERT\",\"fieldName\":\"zonedDateTime\",\"currentValue\":\"2020-01-02T03:04:05\",\"destinationClassName\":\"ZonedDateTime\"}",
                    response.body().string());
        }
    }

    @Test
    public void postFormParamSimpleNoLocalDate() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-simple")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserDTO obj = gson.fromJson(response.body().string(), SpecialParserDTO.class);
            assertEquals(333L, obj.getLongValue());
            assertEquals(DayOfWeek.MONDAY, obj.getDayOfWeek());
            assertNull(obj.getLocalDate());
            assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), obj.getLocalDateTime());
            assertEquals(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)), obj.getZonedDateTime());
        }
    }


    @Test
    public void postFormParamList() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longList", "333")
                .addFormDataPart("longList", "444")
                .addFormDataPart("dayOfWeekList", "MONDAY")
                .addFormDataPart("dayOfWeekList", "TUESDAY")
                .addFormDataPart("localDateList", "2020-01-02")
                .addFormDataPart("localDateList", "2021-01-02")
                .addFormDataPart("localDateTimeList", "2020-01-02T03:04:05")
                .addFormDataPart("localDateTimeList", "2022-01-02T03:04:05")
                .addFormDataPart("zonedDateTimeList", "2020-01-02T03:04:05-04:00")
                .addFormDataPart("zonedDateTimeList", "2023-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-list")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserListDTO obj = gson.fromJson(response.body().string(), SpecialParserListDTO.class);
            assertEquals(List.of(333L, 444L), obj.getLongList());
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), obj.getDayOfWeekList());
            assertEquals(List.of(LocalDate.of(2020, 1, 2), LocalDate.of(2021, 1, 2)), obj.getLocalDateList());
            assertEquals(List.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                    LocalDateTime.of(2022, 1, 2, 3, 4, 5)), obj.getLocalDateTimeList());
            assertEquals(List.of(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)),
                    ZonedDateTime.of(LocalDateTime.of(2023, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0))), obj.getZonedDateTimeList());
        }
    }

    @Test
    public void getFormParamListNoLocalDate() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longList", "333")
                .addFormDataPart("longList", "444")
                .addFormDataPart("dayOfWeekList", "MONDAY")
                .addFormDataPart("dayOfWeekList", "TUESDAY")
                .addFormDataPart("localDateTimeList", "2020-01-02T03:04:05")
                .addFormDataPart("localDateTimeList", "2022-01-02T03:04:05")
                .addFormDataPart("zonedDateTimeList", "2020-01-02T03:04:05-04:00")
                .addFormDataPart("zonedDateTimeList", "2023-01-02T03:04:05-04:00")
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:9999/form-params-list")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserListDTO obj = gson.fromJson(response.body().string(), SpecialParserListDTO.class);
            assertEquals(List.of(333L, 444L), obj.getLongList());
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), obj.getDayOfWeekList());
            assertNull(obj.getLocalDateList());
            assertEquals(List.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                    LocalDateTime.of(2022, 1, 2, 3, 4, 5)), obj.getLocalDateTimeList());
            assertEquals(List.of(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)),
                    ZonedDateTime.of(LocalDateTime.of(2023, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0))), obj.getZonedDateTimeList());
        }
    }

    @Test
    public void postBodyParam() throws IOException {
        final RequestBody body = RequestBody
                .create("{\n"
                                + "    \"longValue\": \"333\",\n"
                                + "    \"dayOfWeek\": \"MONDAY\",\n"
                                + "    \"localDate\": \"2020-01-02\",\n"
                                + "    \"localDateTime\": \"2020-01-02T03:04:05\",\n"
                                + "    \"zonedDateTime\": \"2020-01-02T03:04:05-04:00\"\n"
                                + "}",
                        MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/body-params")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserDTO obj = gson.fromJson(response.body().string(), SpecialParserDTO.class);
            assertEquals(333L, obj.getLongValue());
            assertEquals(DayOfWeek.MONDAY, obj.getDayOfWeek());
            assertEquals(LocalDate.of(2020, 1, 2), obj.getLocalDate());
            assertEquals(LocalDateTime.of(2020, 1, 2, 3, 4, 5), obj.getLocalDateTime());
            assertEquals(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)), obj.getZonedDateTime());
        }
    }

    @Test
    public void postBodyParamAllNullButDeclared() throws IOException {
        final RequestBody body = RequestBody
                .create("{\n"
                                + "    \"longValue\": null,\n"
                                + "    \"dayOfWeek\": null,\n"
                                + "    \"localDate\": null,\n"
                                + "    \"localDateTime\": null,\n"
                                + "    \"zonedDateTime\": null\n"
                                + "}",
                        MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/body-params")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserDTO obj = gson.fromJson(response.body().string(), SpecialParserDTO.class);
            assertNull(obj.getLongValue());
            assertNull(obj.getDayOfWeek());
            assertNull(obj.getLocalDate());
            assertNull(obj.getLocalDateTime());
            assertNull(obj.getZonedDateTime());
        }
    }

    @Test
    public void postBodyParamAllNull() throws IOException {
        final RequestBody body = RequestBody
                .create("{}",
                        MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/body-params")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserDTO obj = gson.fromJson(response.body().string(), SpecialParserDTO.class);
            assertNull(obj.getLongValue());
            assertNull(obj.getDayOfWeek());
            assertNull(obj.getLocalDate());
            assertNull(obj.getLocalDateTime());
            assertNull(obj.getZonedDateTime());
        }
    }

    @Test
    public void postBodyParamWrongEnum() throws IOException {
        final RequestBody body = RequestBody
                .create("{\n"
                                + "    \"longValue\": \"333\",\n"
                                + "    \"dayOfWeek\": \"MONxxxxxDAY\",\n"
                                + "    \"localDate\": \"2020-01-02\",\n"
                                + "    \"localDateTime\": \"2020-01-02T03:04:05\",\n"
                                + "    \"zonedDateTime\": \"2020-01-02T03:04:05-04:00\"\n"
                                + "}",
                        MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("http://localhost:9999/body-params")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            assertEquals("{\"type\":\"com.pinframework.exceptions.PinBadRequestException\",\"message\":\"java.lang.IllegalArgumentException: Can not deserialize MONxxxxxDAY to java.time.DayOfWeek\",\"messageKey\":\"CAN_NOT_PARSE\"}",
                    response.body().string());
        }
    }

    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }

}