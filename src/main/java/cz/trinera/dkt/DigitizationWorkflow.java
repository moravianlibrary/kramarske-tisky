package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetector.Barcode;
import cz.trinera.dkt.jp2k.Jp2kConvertor;
import cz.trinera.dkt.ocr.OcrProvider;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DigitizationWorkflow {

    private final Integer MAX_BLOCKS_TO_PROCESS = 1; //TODO: set to NULL in production

    private final BarcodeDetector barcodeDetector;
    private final OcrProvider ocrProvider;
    private final Jp2kConvertor jp2kConvertor;

    public DigitizationWorkflow(BarcodeDetector barcodeDetector, OcrProvider ocrProvider, Jp2kConvertor jp2kConvertor) {
        this.barcodeDetector = barcodeDetector;
        this.ocrProvider = ocrProvider;
        this.jp2kConvertor = jp2kConvertor;
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

        int processedBlocks = 0;
        for (File file : listImageFiles(inputDir)) {
            if (MAX_BLOCKS_TO_PROCESS != null && processedBlocks >= MAX_BLOCKS_TO_PROCESS) {
                System.out.println("Limit MAX_BLOCKS_TO_PROCESS reached (" + MAX_BLOCKS_TO_PROCESS + " blocks), quitting now");
                return;
            }
            if (file.getName().endsWith(".png")) {
                //System.out.println("Listing " + file.getName());
                BarcodeDetector.Barcode barcode = barcodeDetector.detect(file);
                if (barcode != null) {
                    if (lastBarcode == null) {
                        lastBarcode = barcode;
                    } else {
                        //next barcode found - process the block of all images before that
                        if (MAX_BLOCKS_TO_PROCESS == null || processedBlocks < MAX_BLOCKS_TO_PROCESS) {
                            processBlockPhase1(imagesToBeProcessed, lastBarcode, workingDir, outputDir);
                            processedBlocks++;
                        }
                        //set new barcode as last and reset images to be processed
                        lastBarcode = barcode;
                        imagesToBeProcessed.clear();
                    }
                } else {
                    imagesToBeProcessed.add(file);
                }
            }
        }
        if (lastBarcode != null) { //process the last block
            if (MAX_BLOCKS_TO_PROCESS == null || processedBlocks < MAX_BLOCKS_TO_PROCESS) {
                processBlockPhase1(imagesToBeProcessed, lastBarcode, workingDir, outputDir);
            }
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

    /**
     * Checks image files, if they are valid block of pages (4, 8, 16 pages), create
     */
    private void processBlockPhase1(List<File> imagesToBeProcessed, BarcodeDetector.Barcode barcode, File workingDir, File outputDir) {
        String imageFilesStr = imagesToBeProcessed.stream().map(File::getName).reduce((a, b) -> a + ", " + b).orElse("");
        System.out.println("Processing " + imagesToBeProcessed.size() + " files with barcode " + barcode.getValue() + ": " + imageFilesStr);
        //test if correct number of pages
        int[] expectedNumbersOfPages = {4, 8, 16}; //musí být sudý počet stránek
        if (Arrays.stream(expectedNumbersOfPages).noneMatch(count -> count == imagesToBeProcessed.size())) {
            System.out.println("Invalid number of pages in block: " + imagesToBeProcessed.size() + ", ignoring block with barcode " + barcode.getValue());
            return;
        }
        //name pages: 1r, 1v, 2r, 2v, ...
        List<NamedPage> pages = new ArrayList<>();
        for (int i = 0, num = 1; i < imagesToBeProcessed.size(); i++) {
            char side = i % 2 == 0 ? 'r' : 'v';
            String pageName = "" + num + side;
            pages.add(new NamedPage(i + 1, pageName, imagesToBeProcessed.get(i)));
            if (i % 2 == 1) {
                num++;
            }
        }

        //create working dir and fill with copies of original images
        Timestamp ts = Timestamp.from(Instant.now());
        String timestampFormatted = ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss,SSS"));
        File blockWorkingDir = new File(workingDir, barcode.getValue() + "-" + timestampFormatted);
        makeSureReadableWritableDirExists(blockWorkingDir);
        File blockWorkingDirImagesDir = new File(blockWorkingDir, "images-in");
        makeSureReadableWritableDirExists(blockWorkingDirImagesDir);
        List<NamedPage> pagesInWorkingDir = new ArrayList<>();
        for (NamedPage page : pages) {
            File pageDestFile = new File(blockWorkingDirImagesDir, page.getPosition() + ".png");
            //System.out.println("Copying " + page.getImageFile().getAbsolutePath() + " to " + pageDestFile.getAbsolutePath());
            Utils.copyFile(page.getImageFile(), pageDestFile);
            pagesInWorkingDir.add(page.withDifferentFile(pageDestFile));
        }

        //continue with phase 2
        processBlockPhase2(pagesInWorkingDir, barcode, blockWorkingDir, outputDir);
    }

    /*
     * Process images in working dir (copy of original images)
     */
    private void processBlockPhase2(List<NamedPage> pages, Barcode barcode, File blockWorkingDir, File outputDir) {
        //fetch OCR (text, alto) for each page
        File ocrTextDir = new File(blockWorkingDir, "ocr-text");
        makeSureReadableWritableDirExists(ocrTextDir);
        File ocrAltoDir = new File(blockWorkingDir, "ocr-alto");
        makeSureReadableWritableDirExists(ocrAltoDir);
        for (NamedPage page : pages) {
            //System.out.println(page);
            File ocrTextFile = new File(ocrTextDir, page.getPosition() + ".txt");
            File ocrAltoFile = new File(ocrAltoDir, page.getPosition() + ".xml");
            ocrProvider.fetchOcr(page.getImageFile(), ocrTextFile, ocrAltoFile);
        }

        //convert each page to jp2k (user copy, archive copy)
        File jp2kUserCopyDir = new File(blockWorkingDir, "jp2k-usercopy");
        makeSureReadableWritableDirExists(jp2kUserCopyDir);
        File jp2kArchiveCopyDir = new File(blockWorkingDir, "jp2k-archivecopy");
        makeSureReadableWritableDirExists(jp2kArchiveCopyDir);
        for (NamedPage page : pages) {
            File jp2kUserCopyFile = new File(jp2kUserCopyDir, page.getPosition() + ".jp2");
            File jp2kArchiveCopyFile = new File(jp2kArchiveCopyDir, page.getPosition() + ".jp2");
            jp2kConvertor.convertToJp2k(page.getImageFile(), jp2kUserCopyFile, jp2kArchiveCopyFile);
        }


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
