package cz.trinera.dkt.ocr;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;
import java.util.logging.Logger;

public class OcrProviderImpl implements OcrProvider {

    private final Logger logger = Logger.getLogger(OcrProviderImpl.class.getName());

    //TODO: use url and api key from configuration
    private String baseUrl = "https://pero-ocr.fit.vutbr.cz/api";
    private String apiKey = "API_KEY";

    private final int engineId;

    public OcrProviderImpl(int engineId) {
        this.engineId = engineId;
        logger.info("Initializing OCR provider with engine ID " + engineId);
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //TODO: implement
        throw new ToolAvailabilityError("OCR provider is not available");
        //TODO: check engines and that our engine is available
    }

    @Override
    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile) {
        logger.info("Fetching OCR for image " + inImgFile.getName());
        //TODO: implement
    }

}
