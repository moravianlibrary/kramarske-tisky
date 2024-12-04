package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorPyzbar;
import org.junit.jupiter.api.Test;

import java.io.File;

import static cz.trinera.dkt.barcode.BarcodeDetector.Barcode;
import static org.junit.jupiter.api.Assertions.*;

public class BarcodeDetectorTest {

    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png";
    //private final BarcodeDetector barcodeDetector = new BarcodeDetectorImplZxing();
    //private final BarcodeDetector barcodeDetector = new BarcodeDetectorImplBoofCv();
    private final BarcodeDetector barcodeDetector = new BarcodeDetectorPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");


    @Test
    public void checkBarcodeDetector() {
        try {
            barcodeDetector.checkAvailable();
        } catch (ToolAvailabilityError e) {
            fail(e);
        }
    }

    @Test
    public void testBarcodeYes1() {
        File file = new File(sampleDir + "/0001.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
    }

    @Test
    public void testBarcodeYes2() {
        File file = new File(sampleDir + "/0010.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
    }

    @Test
    public void testBarcodeYes3() {
        File file = new File(sampleDir + "/0019.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
    }

    @Test
    public void testBarcodeYes4() {
        File file = new File(sampleDir + "/0028.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
    }

    @Test
    public void testBarcodeYes5() {
        File file = new File(sampleDir + "/0037.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
    }

    @Test
    public void testBarcodeNo1() {
        File file = new File(sampleDir + "/0002.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

    @Test
    public void testBarcodeNo2() {
        File file = new File(sampleDir + "/0003.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

    @Test
    public void testBarcodeNo3() {
        File file = new File(sampleDir + "/0004.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

    @Test
    public void testBarcodeNo4() {
        File file = new File(sampleDir + "/0005.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

    @Test
    public void testBarcodeNo5() {
        File file = new File(sampleDir + "/0006.png");
        System.out.println("file = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

}
