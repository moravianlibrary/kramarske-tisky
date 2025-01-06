package cz.trinera.dkt;

import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import nu.xom.Document;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class OcrProviderTest {

    private String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";

    private final int engineId = 1; //czech_old_printed
    //private final int engineId = 2; //czech_old_handwritten
    //private final int engineId = 3; //arabic_handwritten
    //private final int engineId = 4;//german_fraktur_printed
    //private final int engineId = 5; //german_fraktur_printed--visual_transcription_style
    //private final int engineId = 6; //european_printed_modern_german_style
    //private final int engineId = 7; //modern_universal_printed

    //OcrProvider ocrProvider = new OcrProviderMock();
    OcrProvider ocrProvider = new OcrProviderImpl(
            "https://pero-ocr.fit.vutbr.cz/api",
            "API_KEY",
            engineId);


    @Test
    public void getOcr() {
        //prepare
        System.out.println("sample dir: " + sampleDir);
        File inImageFile = new File(sampleDir + "/0003.png");
        File outTextFile = new File(sampleDir + "/0003.txt");
        File outAltoFile = new File(sampleDir + "/0003.xml");
        //clean up
        outTextFile.delete();
        outAltoFile.delete();
        //convert
        ocrProvider.fetchOcr(inImageFile, outTextFile, outAltoFile);
        //check results - text
        assertTrue(outTextFile.exists());
        long textFileBytes = outTextFile.length();
        assertTrue(textFileBytes > 0);
        //check results - alto
        assertTrue(outAltoFile.exists());
        long altoFileBytes = outAltoFile.length();
        assertTrue(altoFileBytes > 0);
        Document altoDoc = Utils.loadXmlFromFile(outAltoFile);
        assertEquals("alto", altoDoc.getRootElement().getLocalName());
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
