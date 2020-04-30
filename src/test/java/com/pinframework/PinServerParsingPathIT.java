package com.pinframework;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.gson.Gson;
import com.pinframework.json.PinGsonBuilderFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PinServerParsingPathIT {

    private PinServer pinServer;

    private final OkHttpClient client = new OkHttpClient();

    private final Gson gson = PinGsonBuilderFactory.make().create();

    @BeforeAll
    public void setup() {
        pinServer = new PinServerBuilder().build();
        pinServer.onGet("path-params/:longValue1/inter1/:longValue2", ex -> {
            Long longValue1 = ex.getPathParamAsLong("longValue1");
            Long longValue2 = ex.getPathParamAsLong("longValue2");

            return PinResponse.ok(longValue1 + longValue2);

        });

        //        pinServer.onGet("path-params/:longValue/inter1/:dayOfWeek/inter2/:localDate/inter3/:localDateTime/inter4/:zonedDateTime", ex -> {
        //            Long longValue = ex.getPathParamAsLong("longValue");
        //            DayOfWeek dayOfWeek = ex.getPathParamAsEnum("dayOfWeek", DayOfWeek.class);
        //            LocalDate localDate = ex.getPathParamAsLocalDate("localDate");
        //            LocalDateTime localDateTime = ex.getPathParamAsLocalDateTime("localDateTime");
        //            ZonedDateTime zonedDateTime = ex.getPathParamAsZonedDateTime("zonedDateTime");
        //
        //            var obj = new SpecialParserDTO();
        //            obj.setLongValue(longValue);
        //            obj.setDayOfWeek(dayOfWeek);
        //            obj.setLocalDate(localDate);
        //            obj.setLocalDateTime(localDateTime);
        //            obj.setZonedDateTime(zonedDateTime);
        //
        //            return PinResponse.ok(obj);
        //
        //        });

        pinServer.start();
    }

    @Test
    public void getPathParamSimple() throws IOException {
        Request request = new Request.Builder()
                .url("http://localhost:9999/path-params/333/inter1/444")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(HttpURLConnection.HTTP_OK, response.code());
            assertEquals(PinContentType.APPLICATION_JSON_UTF8, response.header(PinContentType.CONTENT_TYPE));
            Long longResponse = gson.fromJson(response.body().string(), Long.class);
            assertEquals(777L, longResponse);
        }
    }

    @AfterAll
    public void tearDown() {
        pinServer.stop(1);
    }

}