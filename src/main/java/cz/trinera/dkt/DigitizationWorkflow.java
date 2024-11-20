package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetector.Barcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Barcode lastBarcode = null;
        List<File> toBeProcessed = new ArrayList<>();

        for (File file : listImageFiles(inputDir)) {
            if (file.getName().endsWith(".png")) {
                System.out.println("Listing " + file.getName());
                BarcodeDetector.Barcode barcode = barcodeDetector.detect(file);
                if (barcode != null) {
                    if (lastBarcode == null) {
                        lastBarcode = barcode;
                    } else {
                        //found next barcode
                        processBlock(toBeProcessed, lastBarcode);
                        //reset and save the new barcode
                        lastBarcode = barcode;
                        toBeProcessed.clear();
                    }
                } else {
                    toBeProcessed.add(file);
                }
            }
        }
        if (lastBarcode != null) {
            processBlock(toBeProcessed, lastBarcode);
        }
    }

    //TODO: tohle bude kramarsky tisk
    private void processBlock(List<File> toBeProcessed, BarcodeDetector.Barcode barcode) {
        String filesStr = toBeProcessed.stream().map(File::getName).reduce((a, b) -> a + ", " + b).orElse("");
        System.out.println("Processing " + toBeProcessed.size() + " files with barcode " + barcode.getValue() + ": " + filesStr);
        //TODO: test, if correct number of files (4, 8, 16)
        //TODO: process files
    }

    private File[] listImageFiles(File inputDir) {
        //System.out.println("Listing files in " + inputDir);
        if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory()) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + inputDir);
        }
        return Arrays
                .stream(inputDir.listFiles((dir, name) -> name.matches("\\d+\\.png")))
                .sorted((first, second) -> {
                    int nFirst = Integer.parseInt(first.getName().split("\\.")[0]);
                    int nSecond = Integer.parseInt(second.getName().split("\\.")[0]);
                    return Integer.compare(nFirst, nSecond);
                }).toArray(File[]::new);
    }
}
