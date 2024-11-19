package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class DigitizationWorkflow {

    private final BarcodeDetector barcodeDetector;

    public DigitizationWorkflow(BarcodeDetector barcodeDetector) {
        this.barcodeDetector = barcodeDetector;
    }

    /**
     * Reads file names from inputDir, sorts them by number and processes them
     * File must be named as a number with a .png extension
     *
     * @param inputDir directory with png files
     */
    public void run(File inputDir) {
        System.out.println("Running digitization workflow on " + inputDir);
        //process all png files in the directory inputDir
        for (File file : Arrays.stream(Objects.requireNonNull(
                inputDir.listFiles((dir, name) -> name.matches("\\d+\\.png")))
        ).sorted((first, second) -> {
            int nFirst = Integer.parseInt(first.getName().split("\\.")[0]);
            int nSecond = Integer.parseInt(second.getName().split("\\.")[0]);
            return Integer.compare(nFirst, nSecond);
        }).toArray(File[]::new)) {
            if (file.getName().endsWith(".png")) {
                System.out.println("Processing " + file.getName());
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
