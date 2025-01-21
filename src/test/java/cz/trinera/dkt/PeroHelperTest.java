package cz.trinera.dkt;

import cz.trinera.dkt.ocr.PeroHelper;
import cz.trinera.dkt.utils.ApiResponse;
import nu.xom.Document;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PeroHelperTest {

    private final boolean testsDisabled = false;
    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";

    private final PeroHelper peroHelper;

    public PeroHelperTest() {
        try {
            File homeDir = new File(System.getProperty("user.home"));
            File configFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/config.properties");
            Config.init(configFile);
            this.peroHelper = new PeroHelper(
                    Config.instanceOf().getOcrProviderPeroBaseUrl(),
                    Config.instanceOf().getOcrProviderPeroApiKey());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
            //Supported formats are jpeg, jpg, tif, jp2, png, jpf, j2c, jpx, bmp, mj2, tiff, jpg2, j2k, jpm, jpc
            File imageFile = new File(sampleDir + "/0005.png");
            System.out.println("uploading image... " + imageFile.getAbsolutePath());
            ApiResponse response = peroHelper.queryPostMultipart("upload_image/" + requestId + "/" + pageId, imageFile);
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);

            System.out.println("response data:");
            System.out.println(responseDataJson.toString(2));
            assertTrue(responseDataJson.has("status"));
            assertEquals("success", responseDataJson.getString("status"));
        } catch (IOException e) {
            fail(e);
        }
    }

    private void uploadImageFromFile(String requestId, String pageId, File imageFile) throws IOException {
        ApiResponse response = peroHelper.queryPostMultipart("upload_image/" + requestId + "/" + pageId, imageFile);
        String responseData = response.result.toString();
        JSONObject responseDataJson = new JSONObject(responseData);
        System.out.println("image uploaded: ");
        System.out.println(responseDataJson.toString(2));
    }

    private JSONObject checkStatus(String requestId) throws IOException {
        ApiResponse response = peroHelper.queryGet("request_status/" + requestId);
        assertTrue(response.isOk());
        String responseData = response.result.toString();
        return new JSONObject(responseData);
    }

    @Test
    public void uploadImageAndFetchResults() {
        if (testsDisabled) {
            return;
        }
        try {
            //create request with one page
            System.out.println("creating request...");
            String pageId = "page_1";
            String requestId = createProcessingRequest(1, List.of(pageId));
            System.out.println("requestId: " + requestId);
            System.out.println("pageId: " + pageId);

            //upload image
            System.out.println("uploading image...");
            File inFile = new File(sampleDir + "/0005.png");
            uploadImageFromFile(requestId, pageId, inFile);

            //check status
            boolean processingFinished = false;
            boolean processedOk = false;
            while (!processingFinished) {
                System.out.println("checking status...");
                JSONObject requestStatus = checkStatus(requestId);
                System.out.println("requestStatus:");
                System.out.println(requestStatus.toString(2));
                JSONObject page = requestStatus.getJSONObject("request_status").getJSONObject(pageId);
                String state = page.getString("state");
                if (state.equals("PROCESSED")) {
                    processingFinished = true;
                    processedOk = true;
                    System.out.println("processing finished");
                } else if (state.equals("INVALID_FILE")) {
                    processingFinished = true;
                    processedOk = false;
                    System.out.println("processing finished with error");
                    fail();
                } else {
                    System.out.println("processing not finished (state " + state + "), waiting...");
                    Thread.sleep(3000);
                }
            }

            //fetch results, check them
            if (processedOk) {
                //alto
                Object resultsAlto = fetchResult(requestId, pageId, "alto");
                assertNotNull(resultsAlto);
                assertTrue(resultsAlto instanceof Document);
                Document resultsAltoDoc = (Document) resultsAlto;
                System.out.println("results alto:");
                System.out.println(resultsAltoDoc.toXML());

                //txt
                Object resultsTxt = fetchResult(requestId, pageId, "txt");
                assertNotNull(resultsTxt);
                assertTrue(resultsTxt instanceof String);
                String resultsTxtStr = (String) resultsTxt;
                assertFalse(resultsTxtStr.isBlank());
                System.out.println("results txt:");
                System.out.println(resultsTxt);
            }
        } catch (IOException e) {
            fail(e);
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    @Test
    public void uploadImageWithoutLettersAndFetchResults() {
        if (testsDisabled) {
            return;
        }
        try {
            //create request with one page
            System.out.println("creating request...");
            String pageId = "page_1";
            String requestId = createProcessingRequest(1, List.of(pageId));
            System.out.println("requestId: " + requestId);
            System.out.println("pageId: " + pageId);

            //upload image
            System.out.println("uploading image...");
            //File inFile = new File(sampleDir + "/0002_no_text.png");
            File inFile = new File("src/test/resources/pero-helper-test/0002_no_text.png");
            uploadImageFromFile(requestId, pageId, inFile);

            //check status
            boolean processingFinished = false;
            boolean processedOk = false;
            while (!processingFinished) {
                System.out.println("checking status...");
                JSONObject requestStatus = checkStatus(requestId);
                System.out.println("requestStatus:");
                System.out.println(requestStatus.toString(2));
                JSONObject page = requestStatus.getJSONObject("request_status").getJSONObject(pageId);
                String state = page.getString("state");
                if (state.equals("PROCESSED")) {
                    processingFinished = true;
                    processedOk = true;
                    System.out.println("processing finished");
                } else if (state.equals("INVALID_FILE")) {
                    processingFinished = true;
                    processedOk = false;
                    System.out.println("processing finished with error");
                    fail();
                } else {
                    System.out.println("processing not finished (state " + state + "), waiting...");
                    Thread.sleep(3000);
                }
            }

            //fetch results, check them
            if (processedOk) {
                //alto
                Object resultsAlto = fetchResult(requestId, pageId, "alto");
                assertNotNull(resultsAlto);
                assertTrue(resultsAlto instanceof Document);
                Document resultsAltoDoc = (Document) resultsAlto;
                System.out.println("results alto:");
                System.out.println(resultsAltoDoc.toXML());

                //txt
                Object resultsTxt = fetchResult(requestId, pageId, "txt");
                assertNotNull(resultsTxt);
                assertTrue(resultsTxt instanceof String);
                String resultsTxtStr = (String) resultsTxt;
                assertTrue(resultsTxtStr.isBlank());
                System.out.println("results txt:");
                System.out.println(resultsTxt);
            }
        } catch (IOException e) {
            fail(e);
        } catch (InterruptedException e) {
            fail(e);
        }
    }

    private Object fetchResult(String requestId, String pageId, String format) throws IOException {
        ApiResponse apiResponse = peroHelper.queryGet("download_results/" + requestId + "/" + pageId + "/" + format);
        assertTrue(apiResponse.isOk());
        return apiResponse.result;
    }

    @Test
    public void fetchResults() {
        if (testsDisabled) {
            return;
        }
        String requestId = "b17aa578-aa69-480d-b4ed-15123f7a501c";
        String pageId = "page_1";
        try {
            //alto
            Object resultsAlto = fetchResult(requestId, pageId, "alto");
            assertTrue(resultsAlto instanceof Document);
            Document resultsAltoDoc = (Document) resultsAlto;
            System.out.println("results alto:");
            System.out.println(resultsAltoDoc.toXML());

            //txt
            Object resultsTxt = fetchResult(requestId, pageId, "txt");
            assertTrue(resultsTxt instanceof String);
            System.out.println("results txt:");
            System.out.println(resultsTxt);
        } catch (IOException e) {
            fail(e);
        }
    }

}
