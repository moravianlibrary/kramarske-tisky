package cz.trinera.dkt;

import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class OcrProviderTest {

    private String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/ocr-test";


    //OcrProvider ocrProvider = new OcrProviderMock();
    OcrProvider ocrProvider = new OcrProviderImpl(
            "https://pero-ocr.fit.vutbr.cz/api",
            "API_KEY",
            //1); //Engine for czech printed newspapers from the begining of the 20. century.
            2); //Engine for historical handwritten documents.


    @Test
    public void getOcr() {
        //prepare
        //sampleDir = new File("src/test/resources/pero-helper-test").getAbsolutePath();
        /*File inImageFile = new File(sampleDir + "/noviny_1909.png");
        File outTextFile = new File(sampleDir + "/noviny_1909.txt");
        File outAltoFile = new File(sampleDir + "/noviny_1909.xml");*/
        System.out.println(sampleDir);
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
