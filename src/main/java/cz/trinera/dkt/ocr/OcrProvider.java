package cz.trinera.dkt.ocr;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public interface OcrProvider {

    public void checkAvailable() throws ToolAvailabilityError;

    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile);

}
