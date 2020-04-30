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
        pinServer.onGet("path-params/:longValue/:dayOfWeek/:localDate/:localDateTime/:zonedDateTime", ex -> {
            Long longValue = ex.getPathParamAsLong("longValue");
            DayOfWeek dayOfWeek = ex.getPathParamAsEnum("dayOfWeek", DayOfWeek.class);
            LocalDate localDate = ex.getPathParamAsLocalDate("localDate");
            LocalDateTime localDateTime = ex.getPathParamAsLocalDateTime("localDateTime");
            ZonedDateTime zonedDateTime = ex.getPathParamAsZonedDateTime("zonedDateTime");

            var obj = new SpecialParserDTO();
            obj.setLongValue(longValue);
            obj.setDayOfWeek(dayOfWeek);
            obj.setLocalDate(localDate);
            obj.setLocalDateTime(localDateTime);
            obj.setZonedDateTime(zonedDateTime);

            return PinResponse.ok(obj);

        });

        pinServer.onGet("query-params-simple", ex -> {
            Long longValue = ex.getQueryParamFirstAsLong("longValue");
            DayOfWeek dayOfWeek = ex.getQueryParamFirstAsEnum("dayOfWeek", DayOfWeek.class);
            LocalDate localDate = ex.getQueryParamFirstAsLocalDate("localDate");
            LocalDateTime localDateTime = ex.getQueryParamFirstAsLocalDateTime("localDateTime");
            ZonedDateTime zonedDateTime = ex.getQueryParamFirstAsZonedDateTime("zonedDateTime");

            var obj = new SpecialParserDTO();
            obj.setLongValue(longValue);
            obj.setDayOfWeek(dayOfWeek);
            obj.setLocalDate(localDate);
            obj.setLocalDateTime(localDateTime);
            obj.setZonedDateTime(zonedDateTime);

            return PinResponse.ok(obj);

        });

        pinServer.onGet("query-params-list", ex -> {
            List<Long> longList = ex.getQueryParamAsLongList("longList");
            List<DayOfWeek> dayOfWeekList = ex.getQueryParamAsEnumList("dayOfWeekList", DayOfWeek.class);
            List<LocalDate> localDateList = ex.getQueryParamAsLocalDateList("localDateList");
            List<LocalDateTime> localDateTimeList = ex.getQueryParamAsLocalDateTimeList("localDateTimeList");
            List<ZonedDateTime> zonedDateTimeList = ex.getQueryParamAsZonedDateTimeList("zonedDateTimeList");

            var obj = new SpecialParserListDTO();
            obj.setLongList(longList);
            obj.setDayOfWeekList(dayOfWeekList);
            obj.setLocalDateList(localDateList);
            obj.setLocalDateTimeList(localDateTimeList);
            obj.setZonedDateTimeList(zonedDateTimeList);

            return PinResponse.ok(obj);

        });

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

        pinServer.start();
    }

    @Test
    public void getPathParamSimple() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/path-params/333/MONDAY/2020-01-02/2020-01-02T03:04:05/2020-01-02T03:04:05-04:00")
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
    public void getPathParamSimpleNoLocalDate() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/path-params/333/MONDAY//2020-01-02T03:04:05/2020-01-02T03:04:05-04:00")
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
    public void getQueryParamSimple() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/query-params-simple?longValue=333&dayOfWeek=MONDAY&localDate=2020-01-02&localDateTime=2020-01-02T03:04:05"
                        + "&zonedDateTime=2020-01-02T03:04:05-04:00")
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
    public void getQueryParamSimpleNoLocalDate() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/query-params-simple?longValue=333&dayOfWeek=MONDAY&localDateTime=2020-01-02T03:04:05"
                        + "&zonedDateTime=2020-01-02T03:04:05-04:00")
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
    public void getQueryParamList() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/query-params-list?longList=333&longList=444"
                        + "&dayOfWeekList=MONDAY&dayOfWeekList=TUESDAY"
                        + "&localDateList=2020-01-02&localDateList=2021-01-02"
                        + "&localDateTimeList=2020-01-02T03:04:05&localDateTimeList=2021-01-02T03:04:05"
                        + "&zonedDateTimeList=2020-01-02T03:04:05-04:00&zonedDateTimeList=2021-01-02T03:04:05+05:00")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserListDTO obj = gson.fromJson(response.body().string(), SpecialParserListDTO.class);
            assertEquals(List.of(333L, 444L), obj.getLongList());
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), obj.getDayOfWeekList());
            assertEquals(List.of(LocalDate.of(2020, 1, 2), LocalDate.of(2021, 1, 2)), obj.getLocalDateList());
            assertEquals(List.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                    LocalDateTime.of(2021, 1, 2, 3, 4, 5)), obj.getLocalDateTimeList());
            assertEquals(List.of(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)),
                    ZonedDateTime.of(LocalDateTime.of(2021, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(5, 0))), obj.getZonedDateTimeList());
        }
    }

    @Test
    public void getQueryParamListNoLocalDate() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/query-params-list?longList=333&longList=444"
                        + "&dayOfWeekList=MONDAY&dayOfWeekList=TUESDAY"
                        + "&localDateTimeList=2020-01-02T03:04:05&localDateTimeList=2021-01-02T03:04:05"
                        + "&zonedDateTimeList=2020-01-02T03:04:05-04:00&zonedDateTimeList=2021-01-02T03:04:05+05:00")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            SpecialParserListDTO obj = gson.fromJson(response.body().string(), SpecialParserListDTO.class);
            assertEquals(List.of(333L, 444L), obj.getLongList());
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), obj.getDayOfWeekList());
            assertNull(obj.getLocalDateList());
            assertEquals(List.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                    LocalDateTime.of(2021, 1, 2, 3, 4, 5)), obj.getLocalDateTimeList());
            assertEquals(List.of(ZonedDateTime.of(LocalDateTime.of(2020, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(-4, 0)),
                    ZonedDateTime.of(LocalDateTime.of(2021, 1, 2, 3, 4, 5), ZoneOffset.ofHoursMinutes(5, 0))), obj.getZonedDateTimeList());
        }
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

        Request request =  new Request.Builder()
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
    public void postFormParamSimpleNoLocalDate() throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("longValue", "333")
                .addFormDataPart("dayOfWeek", "MONDAY")
                .addFormDataPart("localDateTime", "2020-01-02T03:04:05")
                .addFormDataPart("zonedDateTime", "2020-01-02T03:04:05-04:00")
                .build();

        Request request =  new Request.Builder()
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

        Request request =  new Request.Builder()
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

        Request request =  new Request.Builder()
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


    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }

}