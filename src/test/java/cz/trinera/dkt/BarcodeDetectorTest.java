package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorImplPyzbar;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static cz.trinera.dkt.barcode.BarcodeDetector.Barcode;
import static org.junit.jupiter.api.Assertions.*;

public class BarcodeDetectorTest {

    private final String sampleDir = new File(System.getProperty("user.home")).getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png";
    //private final BarcodeDetector barcodeDetector = new BarcodeDetectorImplZxing();
    //private final BarcodeDetector barcodeDetector = new BarcodeDetectorImplBoofCv();
    private final BarcodeDetector barcodeDetector = new BarcodeDetectorImplPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");

    private static final String FORMAT_CODE39 = "CODE39";
    private static final List<Integer> PAGES_WITH_BARCODE = Arrays.asList(1, 10, 19, 28, 37, 46, 55, 64, 73, 82);

    @Test
    public void checkBarcodeDetector() {
        try {
            barcodeDetector.checkAvailable();
        } catch (ToolAvailabilityError e) {
            fail(e.getMessage(), e);
        }
    }

    @Test
    public void testBarcodeYes1() {
        File file = new File(sampleDir + "/0001.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798805", detected.getValue());
    }

    @Test
    public void testBarcodeYes10() {
        File file = new File(sampleDir + "/0010.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798806", detected.getValue());
    }

    @Test
    public void testBarcodeYes19() {
        File file = new File(sampleDir + "/0019.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798810", detected.getValue());
    }

    @Test
    public void testBarcodeYes28() {
        File file = new File(sampleDir + "/0028.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798809", detected.getValue());
    }

    @Test
    public void testBarcodeYes37() {
        File file = new File(sampleDir + "/0037.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798808", detected.getValue());
    }

    @Test
    public void testBarcodeYes46() {
        File file = new File(sampleDir + "/0046.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798807", detected.getValue());
    }

    @Test
    public void testBarcodeYes55() {
        File file = new File(sampleDir + "/0055.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798803", detected.getValue());
    }

    @Test
    public void testBarcodeYes64() {
        File file = new File(sampleDir + "/0064.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798804", detected.getValue());
    }

    @Test
    public void testBarcodeYes73() {
        File file = new File(sampleDir + "/0073.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798798", detected.getValue());
    }

    @Test
    public void testBarcodeYes82() {
        File file = new File(sampleDir + "/0082.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNotNull(detected);
        assertEquals(FORMAT_CODE39, detected.getFormat());
        assertEquals("2610798797", detected.getValue());
    }

    @Test
    public void testBarcodeNo2() {
        File file = new File(sampleDir + "/0002.png");
        System.out.println("pngFile = " + file);
        Barcode detected = barcodeDetector.detect(file);
        assertNull(detected);
    }

    @Test
    public void testPagesWithoutBarcode() {
        //list all png files in sampleDir
        File folder = new File(sampleDir);
        File[] pngFiles = folder.listFiles((dir, name) -> name.endsWith(".png"));
        for (File pngFile : pngFiles) {
            int pageNumber = Integer.parseInt(pngFile.getName().substring(0, 4));
            if (!PAGES_WITH_BARCODE.contains(pageNumber)) { //exclude images with barcode
                Barcode detected = barcodeDetector.detect(pngFile);
                System.out.println("pngFile = " + pngFile);
                assertNull(detected);
            }
        }
    }

}
