package cz.trinera.dkt.ocr;

import cz.trinera.dkt.AvailabilityError;

import java.io.File;

public interface OcrProvider {

    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile);

    public void checkAvailable() throws AvailabilityError;
}
