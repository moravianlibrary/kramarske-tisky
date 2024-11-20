package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File homeDir = new File(System.getProperty("user.home"));
        File sampleDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png");
        BarcodeDetector barcodeDetector = new BarcodeDetectorMock();
        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector);
        digitizationWorkflow.run(sampleDir);
    }
}