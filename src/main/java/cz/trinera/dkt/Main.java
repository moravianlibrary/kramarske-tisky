package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;

import java.io.File;

public class Main {
    public static void main(String[] args) {

        File sampleDir = new File("/Users/martinrehanek/TrineraProjects/KramarskeTisky/data/input/orezane-png");
        BarcodeDetector barcodeDetector = new BarcodeDetectorMock();
        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector);
        digitizationWorkflow.run(sampleDir);
    }
}