package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;

import java.io.File;

public class DigitizationWorkflow {

    private final BarcodeDetector barcodeDetector;

    public DigitizationWorkflow(BarcodeDetector barcodeDetector) {
        this.barcodeDetector = barcodeDetector;
    }

    public void run(File inputDir) {
        System.out.println("Running digitization workflow on " + inputDir);
        //process all png files in the directory inputDir
        for (File file : inputDir.listFiles((dir, name) -> name.endsWith(".png"))) {
            if (file.getName().endsWith(".png")) {
                System.out.println("Processing " + file);
                BarcodeDetector.Barcode barcode = barcodeDetector.detect(file);
                if (barcode != null) {
                    System.out.println("Found barcode: " + barcode.getFormat() + " " + barcode.getValue());
                } else {
                    System.out.println("No barcode found in " + file);
                }
            }
        }

    }
}
