package cz.trinera.dkt.ocr;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;
import java.util.logging.Logger;

public class OcrProviderImpl implements OcrProvider {

    private final Logger logger = Logger.getLogger(OcrProviderImpl.class.getName());

    private final int engineId;

    public OcrProviderImpl(int engineId) {
        this.engineId = engineId;
        logger.info("Initializing OCR provider with engine ID " + engineId);
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //TODO: implement
    }

    @Override
    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile) {
        logger.info("Fetching OCR for image " + inImgFile.getName());
        //TODO: implement
    }

}
