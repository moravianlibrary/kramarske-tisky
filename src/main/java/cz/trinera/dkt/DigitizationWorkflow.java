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
    public void run(File inputDir, File workingDir, File outputDir) {
        System.out.println("Running digitization workflow ");
        System.out.println("Input dir: " + inputDir.getAbsolutePath());
        makeSureReadableWritableDirExists(inputDir);
        System.out.println("Working dir: " + workingDir.getAbsolutePath());
        makeSureReadableWritableDirExists(workingDir);
        System.out.println("Output dir: " + outputDir.getAbsolutePath());
        makeSureReadableWritableDirExists(outputDir);

        //process all png files in the directory inputDir
        Barcode lastBarcode = null;
        List<File> imagesToBeProcessed = new ArrayList<>();

        for (File file : listImageFiles(inputDir)) {
            if (file.getName().endsWith(".png")) {
                System.out.println("Listing " + file.getName());
                BarcodeDetector.Barcode barcode = barcodeDetector.detect(file);
                if (barcode != null) {
                    if (lastBarcode == null) {
                        lastBarcode = barcode;
                    } else {
                        //found next barcode
                        processBlock(imagesToBeProcessed, lastBarcode);
                        //reset and save the new barcode
                        lastBarcode = barcode;
                        imagesToBeProcessed.clear();
                    }
                } else {
                    imagesToBeProcessed.add(file);
                }
            }
        }
        if (lastBarcode != null) {
            processBlock(imagesToBeProcessed, lastBarcode);
        }
    }

    private void makeSureReadableWritableDirExists(File dir) {
        if (!dir.exists()) {
            System.out.println("Creating directory: " + dir);
            if (!dir.mkdirs()) {
                throw new IllegalArgumentException("Cannot create directory: " + dir);
            }
        }
        //check access rights: can read, write
        if (!dir.canRead() || !dir.canWrite()) {
            throw new IllegalArgumentException("Cannot read or write to directory: " + dir);
        }
    }

    private void processBlock(List<File> toBeProcessed, BarcodeDetector.Barcode barcode) {
        String filesStr = toBeProcessed.stream().map(File::getName).reduce((a, b) -> a + ", " + b).orElse("");
        System.out.println("Processing " + toBeProcessed.size() + " files with barcode " + barcode.getValue() + ": " + filesStr);
        //test if correct number of pages
        int[] expectedNumbersOfPages = {4, 8, 16}; //musí být sudý počet stránek
        if (Arrays.stream(expectedNumbersOfPages).noneMatch(count -> count == toBeProcessed.size())) {
            System.out.println("Invalid number of pages in block: " + toBeProcessed.size() + ", ignoring block with barcode " + barcode.getValue());
            return;
        }
        //name pages: 1r, 1v, 2r, 2v, ...
        List<NamedPage> pages = new ArrayList<>();
        for (int i = 0, num = 1; i < toBeProcessed.size(); i++) {
            char side = i % 2 == 0 ? 'r' : 'v';
            String pageName = "" + num + side;
            pages.add(new NamedPage(i + 1, pageName, toBeProcessed.get(i)));
            if (i % 2 == 1) {
                num++;
            }
        }
        //fetch OCR
        for (NamedPage page : pages) {
            //TODO: fetch OCR from Pero and enrich NamedPage
        }
        //TODO: convert each page to jp2k (archivni, uzivatelska kopie)

        //TODO: for whole block: fetch MARC21 from Aleph (Z39.50) by barcode and convert to MODS (vcetne rozsirene sablony)
        //TODO: build NDK package, move to outputDir
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
