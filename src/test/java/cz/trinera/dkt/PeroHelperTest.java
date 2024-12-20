package cz.trinera.dkt;

import cz.trinera.dkt.ocr.PeroHelper;
import cz.trinera.dkt.utils.ApiResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PeroHelperTest {

    private final boolean testsDisabled = true;
    private final String baseUrl = "https://pero-ocr.fit.vutbr.cz/api";
    private final String apiKey = "API_KEY";
    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";

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
            JSONObject images = new JSONObject();
            images.put("page_1", JSONObject.NULL);
            data.put("images", images);

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
        } catch (IOException e) {
            fail(e);
        }
    }

    private String createProcessingRequest(int engine, List<String> pageIds) throws IOException {
        JSONObject data = new JSONObject();
        data.put("engine", engine);
        JSONObject images = new JSONObject();
        for (String pageId : pageIds) {
            images.put(pageId, JSONObject.NULL);
        }
        data.put("images", images);
        //System.out.println("request data:");
        //System.out.println(data.toString(2));
        ApiResponse response = peroHelper.queryPost("post_processing_request", data);
        String responseData = response.result.toString();
        JSONObject responseDataJson = new JSONObject(responseData);
        //System.out.println("response data:");
        //System.out.println(responseDataJson.toString(2));
        return responseDataJson.getString("request_id");
    }

    @Test
    public void getStatus() {
        if (testsDisabled) {
            return;
        }
        try {
            //create request with one page
            String singlePageId = "page_1";
            String requestId = createProcessingRequest(1, List.of(singlePageId));
            System.out.println("requestId: " + requestId);

            //check status
            ApiResponse response = peroHelper.queryGet("request_status/" + requestId);
            assertTrue(response.isOk());
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("status"));
            assertEquals("success", responseDataJson.getString("status"));
            assertTrue(responseDataJson.has("request_status"));
            JSONObject requestStatus = responseDataJson.getJSONObject("request_status");
            assertTrue(requestStatus.has(singlePageId));
            JSONObject page1 = requestStatus.getJSONObject(singlePageId);
            assertTrue(page1.has("state"));
            assertEquals("CREATED", page1.getString("state"));
            assertTrue(page1.has("quality"));
            assertEquals(JSONObject.NULL, page1.get("quality"));

        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void uploadImage() {
        if (testsDisabled) {
            return;
        }
        try {
            //create request with one page
            String pageId = "page_1";
            String requestId = createProcessingRequest(1, List.of(pageId));
            System.out.println("requestId: " + requestId);

            //upload image
            File inFile = new File(sampleDir + "/0005.png");
            ApiResponse response = peroHelper.queryPostMultipart("upload_image/" + requestId + "/" + pageId, inFile);
            System.out.println(response.toString());
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);
            assertTrue(response.isOk());

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("status"));
            assertEquals("success", responseDataJson.getString("status"));
        } catch (IOException e) {
            fail(e);
        }
    }

}
