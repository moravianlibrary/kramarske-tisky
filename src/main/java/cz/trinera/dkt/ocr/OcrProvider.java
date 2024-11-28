package cz.trinera.dkt.ocr;

import java.io.File;

public interface OcrProvider {

    public void fetchOcr(File inImgFile, File outTextFile, File outAltoFile);
}
