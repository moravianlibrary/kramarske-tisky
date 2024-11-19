package cz.trinera.dkt;

import java.io.File;

public class Main {
    public static void main(String[] args) {

        File sampleDir = new File("/Users/martinrehanek/TrineraProjects/KramarskeTisky/data/input/orezane-png");
        BarcodeDetector barcodeDetector = new BarcodeDetectorMock();
        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector);
        digitizationWorkflow.run(sampleDir);
    }
}