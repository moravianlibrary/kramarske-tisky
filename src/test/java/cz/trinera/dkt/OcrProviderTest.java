package cz.trinera.dkt;

import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OcrProviderTest {

    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";

    OcrProvider ocrProvider = new OcrProviderMock();
    //OcrProvider ocrProvider = new OcrProviderImpl(1);

    @Test
    public void getOcr1() {
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
        //check results
        assertTrue(outTextFile.exists());
        //TODO: check size, content of outTextFile
        assertTrue(outAltoFile.exists());
        //TODO: check size, content of outAltoFile
    }
}
