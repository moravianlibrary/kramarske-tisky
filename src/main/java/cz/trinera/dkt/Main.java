package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File homeDir = new File(System.getProperty("user.home"));
        File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png");
        File workingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-processing");
        File outputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-results");

        BarcodeDetector barcodeDetector = new BarcodeDetectorMock();
        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector);
        digitizationWorkflow.run(inputDir, workingDir, outputDir);
    }
}