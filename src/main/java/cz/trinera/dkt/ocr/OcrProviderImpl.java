package cz.trinera.dkt.ocr;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;
import cz.trinera.dkt.utils.ApiResponse;
import nu.xom.Document;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class OcrProviderImpl implements OcrProvider {

    private final Logger logger = Logger.getLogger(OcrProviderImpl.class.getName());

    private final int engineId;
    private final PeroHelper peroHelper;

    public OcrProviderImpl(String baseUrl, String apiKey, int engineId) {
        logger.info("Initializing OCR provider with engine ID " + engineId);
        this.peroHelper = new PeroHelper(baseUrl, apiKey);
        this.engineId = engineId;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        try {
            ApiResponse response = peroHelper.queryGet("get_engines");
            if (!response.isOk()) {
                switch (response.responseCode) {
                    case 401:
                        throw new ToolAvailabilityError("OCR provider is not available (response_code=401, unauthorized)");
                    case 403:
                        throw new ToolAvailabilityError("OCR provider is not available (response_code=403, forbidden)");
                    case 404:
                        throw new ToolAvailabilityError("OCR provider is not available (response_code=404, not found)");
                    case 500:
                        throw new ToolAvailabilityError("OCR provider is not available (response_code=500, internal server error)");
                    default:
                        throw new ToolAvailabilityError("OCR provider is not available (response_code=" + response.responseCode + ")");
                }
            }
            String responseData = response.result.toString();
            JSONObject responseDataJson = new JSONObject(responseData);
            String status = responseDataJson.getString("status");
            if (!status.equals("success")) {
                throw new ToolAvailabilityError("OCR provider is not available (status=" + status + ")");
            }
            JSONObject engines = responseDataJson.getJSONObject("engines");
            boolean engineAvailable = false;
            for (String key : engines.keySet()) {
                JSONObject engine = engines.getJSONObject(key);
                if (engine.getInt("id") == engineId) {
                    engineAvailable = true;
                    break;
                }
            }
            if (!engineAvailable) {
                throw new ToolAvailabilityError("OCR engine " + engineId + " is not available");
            }
        } catch (IOException e) {
            throw new ToolAvailabilityError("OCR provider is not available", e);
        }
    }

    @Override
    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile) {
        logger.info("Fetching OCR for image " + inImgFile.getName());
        try {
            //create request with one page
            logger.info("creating request...");
            String pageId = inImgFile.getName().split("\\.")[0];
            String requestId = createProcessingRequest(1, List.of(pageId));
            logger.info("requestId: " + requestId + ", pageId: " + pageId);

            //upload image
            logger.info("uploading image...");
            //File inFile = new File(sampleDir + "/0002_no_text.png");
            File inFile = new File("src/test/resources/pero-helper-test/0002_no_text.png");
            uploadImageFromFile(requestId, pageId, inFile);

            //check status
            boolean processingFinished = false;
            boolean processedOk = false;
            while (!processingFinished) {
                logger.info("checking status...");
                JSONObject requestStatus = checkStatus(requestId);
                //System.out.println("requestStatus:");
                //System.out.println(requestStatus.toString(2));
                JSONObject page = requestStatus.getJSONObject("request_status").getJSONObject(pageId);
                String state = page.getString("state");
                if (state.equals("PROCESSED")) {
                    processingFinished = true;
                    processedOk = true;
                    logger.info("processing finished");
                } else if (state.equals("INVALID_FILE")) {
                    processingFinished = true;
                    processedOk = false;
                    throw new RuntimeException("processing finished with error: INVALID_FILE");
                } else {
                    logger.info("processing not finished (state " + state + "), waiting...");
                    Thread.sleep(3000);
                }
            }

            //fetch results, check them
            if (processedOk) {
                //alto
                Object resultsAlto = fetchResult(requestId, pageId, "alto");
                if (resultsAlto == null) {
                    logger.warning("result alto is null");
                } else if (!(resultsAlto instanceof Document)) {
                    logger.warning("result alto is  not a Document");
                } else {
                    Document resultsAltoDoc = (Document) resultsAlto;
                    //System.out.println("results alto:");
                    //System.out.println(resultsAltoDoc.toXML());
                    Utils.saveDocumentToFile(resultsAltoDoc, outAltoFile);
                }
                //txt
                Object resultsTxt = fetchResult(requestId, pageId, "txt");
                if (resultsTxt == null) {
                    logger.warning("result txt is null");
                } else if (!(resultsTxt instanceof String)) {
                    logger.warning("result txt is not a String");
                } else {
                    String resultsTxtStr = (String) resultsTxt;
                    //System.out.println("results txt:");
                    //System.out.println(resultsTxt);
                    Utils.saveStringToFile(resultsTxtStr, outTextFile);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
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

    private void uploadImageFromFile(String requestId, String pageId, File imageFile) throws IOException {
        ApiResponse response = peroHelper.queryPostMultipart("upload_image/" + requestId + "/" + pageId, imageFile);
        String responseData = response.result.toString();
        JSONObject responseDataJson = new JSONObject(responseData);
    }

    private JSONObject checkStatus(String requestId) throws IOException {
        ApiResponse response = peroHelper.queryGet("request_status/" + requestId);
        if (!response.isOk()) {
            throw new IOException("Error while checking status of request " + requestId + ": " + response.errorMessage);
        }
        String responseData = response.result.toString();
        return new JSONObject(responseData);
    }

    private Object fetchResult(String requestId, String pageId, String format) throws IOException {
        ApiResponse apiResponse = peroHelper.queryGet("download_results/" + requestId + "/" + pageId + "/" + format);
        return apiResponse.result;
    }

}
