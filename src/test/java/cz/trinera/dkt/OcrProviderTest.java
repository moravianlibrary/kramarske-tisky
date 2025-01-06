package cz.trinera.dkt;

import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class OcrProviderTest {

    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";

    //OcrProvider ocrProvider = new OcrProviderMock();
    OcrProvider ocrProvider = new OcrProviderImpl(1);

    @Test
    public void getOcr() {
        //prepare
        System.out.println(sampleDir);
        File inImageFile = new File(sampleDir + "/0001.png");
        File outTextFile = new File(sampleDir + "/0001.txt");
        File outAltoFile = new File(sampleDir + "/0001.xml");
        //clean up
        outTextFile.delete();
        outAltoFile.delete();
        //convert
        ocrProvider.fetchOcr(inImageFile, outTextFile, outAltoFile);
        //check results - text
        assertTrue(outTextFile.exists());
        long textFileBytes = outTextFile.length();
        assertTrue(textFileBytes > 0);
        //check restuls - alto
        assertTrue(outAltoFile.exists());
        long altoFileBytes = outAltoFile.length();
        assertTrue(altoFileBytes > 0);
        //TODO: parse content of alto file as xml
    }

    @Test
    public void checkAvailable() {
        try {
            ocrProvider.checkAvailable();
        } catch (ToolAvailabilityError e) {
            fail(e);
        }
    }
}
