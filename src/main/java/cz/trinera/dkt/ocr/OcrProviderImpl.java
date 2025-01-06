package cz.trinera.dkt.ocr;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.utils.ApiResponse;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
        //TODO: implement
    }

}
