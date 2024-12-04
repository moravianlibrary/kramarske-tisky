package cz.trinera.dkt.ocr;

import cz.trinera.dkt.AvailabilityError;

import java.io.File;
import java.io.IOException;

public class OcrProviderMock implements OcrProvider {

    @Override
    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile) {
        System.out.println("Fetching OCR for image " + inImgFile.getName());
        try {
            outTextFile.createNewFile();
            outAltoFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkAvailable() throws AvailabilityError {
        //do nothing in mock implementation
    }
}
