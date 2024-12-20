package cz.trinera.dkt;

import cz.trinera.dkt.ocr.PeroHelper;
import cz.trinera.dkt.utils.ApiResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class PeroHelperTest {

    private final boolean testsDisabled = true;
    private final String baseUrl = "https://pero-ocr.fit.vutbr.cz/api";
    private final String apiKey = "API_KEY";

    private final PeroHelper peroHelper = new PeroHelper(baseUrl, apiKey);


    @Test
    public void getEngines() {
        if (testsDisabled) {
            return;
        }
        try {
            ApiResponse response = peroHelper.queryGet("get_engines");
            assertTrue(response.isOk());
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("engines"));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void postProcessingRequest() {
        if (testsDisabled) {
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put("engine", 1);
            data.put("images", new JSONObject());

            System.out.println("request data:");
            System.out.println(data.toString(2));
            ApiResponse response = peroHelper.queryPost("post_processing_request", data);
            assertTrue(response.isOk());
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("request_id"));
            assertTrue(responseDataJson.has("status"));
            assertEquals("success", responseDataJson.getString("status"));
            //b20eef74-5015-4154-9346-2a1fea1aa0f7
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void getStatus() {
        if (testsDisabled) {
            return;
        }
        try {
            String requestId = "b20eef74-5015-4154-9346-2a1fea1aa0f7";
            ApiResponse response = peroHelper.queryGet("request_status/" + requestId);
            assertTrue(response.isOk());
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("status"));
            assertEquals("success", responseDataJson.getString("status"));
            assertTrue(responseDataJson.has("request_status"));
            assertEquals("{}", responseDataJson.getJSONObject("request_status").toString());
        } catch (IOException e) {
            fail(e);
        }
    }
}
