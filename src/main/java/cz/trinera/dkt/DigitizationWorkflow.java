package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetector.Barcode;
import cz.trinera.dkt.jp2k.Jp2kConvertor;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc2mods.MarcToModsConvertor;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.tif2png.TifToPngConvertor;
import nu.xom.Document;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DigitizationWorkflow {

    private final Integer MAX_BLOCKS_TO_PROCESS = 1; //TODO: set to NULL in production

    private final TifToPngConvertor tifToPngConvertor;
    private final BarcodeDetector barcodeDetector;
    private final OcrProvider ocrProvider;
    private final Jp2kConvertor jp2kConvertor;
    private final MarcXmlProvider marcXmlProvider;
    private final MarcToModsConvertor marcToModsConvertor;

    public DigitizationWorkflow(TifToPngConvertor tifToPngConvertor, BarcodeDetector barcodeDetector, OcrProvider ocrProvider, Jp2kConvertor jp2kConvertor, MarcXmlProvider marcXmlProvider, MarcToModsConvertor marcToModsConvertor) {
        this.tifToPngConvertor = tifToPngConvertor;
        this.barcodeDetector = barcodeDetector;
        this.ocrProvider = ocrProvider;
        this.jp2kConvertor = jp2kConvertor;
        this.marcXmlProvider = marcXmlProvider;
        this.marcToModsConvertor = marcToModsConvertor;
    }

    /**
     * Reads file names from inputDir, sorts them by number and processes them
     * File must be named as a number with a .png extension
     *
     * @param inputDir directory with png files
     */
    public void run(File inputDir, File pngInputDir, File workingDir, File ndkPackageWorkingDir, File resultsDir) {
        makeSureReadableWritableDirExists(inputDir);
        makeSureReadableWritableDirExists(pngInputDir);
        makeSureReadableWritableDirExists(workingDir);
        makeSureReadableWritableDirExists(ndkPackageWorkingDir);
        makeSureReadableWritableDirExists(resultsDir);

        //convert all tif images to png
        System.out.println("Converting all tif images to png in " + inputDir + " to " + pngInputDir);
        this.tifToPngConvertor.convertAllTifFilesToPng(inputDir, pngInputDir);
        System.out.println("All tif images converted to png");
        System.out.println();

        //process all png files in the directory inputDir
        Barcode lastBarcode = null;
        List<File> imagesToBeProcessed = new ArrayList<>();

        int processedBlocks = 0;
        for (File file : listImageFiles(pngInputDir)) {
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
                            processBlock(imagesToBeProcessed, lastBarcode, workingDir, ndkPackageWorkingDir, resultsDir);
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
                processBlock(imagesToBeProcessed, lastBarcode, workingDir, ndkPackageWorkingDir, resultsDir);
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


    private void processBlock(List<File> imagesToBeProcessed, BarcodeDetector.Barcode barcode, File workingDir, File ndkPackageWorkingDir, File resultsDir) {
        System.out.println();
        System.out.println("Processing new block");
        System.out.println("PHASE 0");
        //blockId
        Timestamp ts = Timestamp.from(Instant.now());
        String timestampFormatted = ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss,SSS"));
        String blockId = barcode.getValue() + "-" + timestampFormatted;

        System.out.println("Processing block with barcode " + barcode.getValue() + ", blockId " + blockId);

        //prepare block dirs
        File workingDirForBlock = new File(workingDir, blockId);
        File ndkPackageWorkingDirForBlock = new File(ndkPackageWorkingDir, blockId);
        File resultsDirForBlock = new File(resultsDir, blockId);

        //phase 1 - check number of pages, name pages, copy to working dir
        List<NamedPage> namedPages = processBlockPhase1(imagesToBeProcessed, barcode, workingDirForBlock);
        if (namedPages == null) {
            return;
        }
        //phase 2 - fetch OCR, convert to jp2k, fetch marc, convert to MODS, convert images to jp2k
        processBlockPhase2(namedPages, barcode, workingDirForBlock);

        //phase 3 - build NDK package
        processBlockPhase3(namedPages, barcode, workingDirForBlock, ndkPackageWorkingDirForBlock);

        //TODO: phase 4: copy result to resultsDir, cleanup
    }


    /**
     * Checks image files, if they are valid block of pages (4, 8, 16 pages), create
     */
    private List<NamedPage> processBlockPhase1(List<File> imagesToBeProcessed, BarcodeDetector.Barcode barcode, File workingDirForBlock) {
        System.out.println("PHASE 1");
        makeSureReadableWritableDirExists(workingDirForBlock);
        String imageFilesStr = imagesToBeProcessed.stream().map(File::getName).reduce((a, b) -> a + ", " + b).orElse("");
        System.out.println("Processing " + imagesToBeProcessed.size() + " files with barcode " + barcode.getValue() + ": " + imageFilesStr);
        //test if correct number of pages
        int[] expectedNumbersOfPages = {4, 8, 16}; //musí být sudý počet stránek
        if (Arrays.stream(expectedNumbersOfPages).noneMatch(count -> count == imagesToBeProcessed.size())) {
            System.out.println("Invalid number of pages in block: " + imagesToBeProcessed.size() + ", ignoring block with barcode " + barcode.getValue());
            return null;
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

        makeSureReadableWritableDirExists(workingDirForBlock);
        File blockWorkingDirImagesDir = new File(workingDirForBlock, "images-in");
        makeSureReadableWritableDirExists(blockWorkingDirImagesDir);
        List<NamedPage> pagesInWorkingDir = new ArrayList<>();
        for (NamedPage page : pages) {
            File pageDestFile = new File(blockWorkingDirImagesDir, page.getPosition() + ".png");
            //System.out.println("Copying " + page.getImageFile().getAbsolutePath() + " to " + pageDestFile.getAbsolutePath());
            Utils.copyFile(page.getImageFile(), pageDestFile);
            pagesInWorkingDir.add(page.withDifferentFile(pageDestFile));
        }
        return pagesInWorkingDir;
    }

    /*
     * Process images in working dir (copy of original images), fetch marc and convert to MODS, fetch OCR, convert images to jp2k
     */
    private void processBlockPhase2(List<NamedPage> pages, Barcode barcode, File workingDirBlock) {
        //fetch OCR (text, alto) for each page
        File ocrTextDir = new File(workingDirBlock, "ocr-text");
        makeSureReadableWritableDirExists(ocrTextDir);
        File ocrAltoDir = new File(workingDirBlock, "ocr-alto");
        makeSureReadableWritableDirExists(ocrAltoDir);
        for (NamedPage page : pages) {
            //System.out.println(page);
            File ocrTextFile = new File(ocrTextDir, page.getPosition() + ".txt");
            File ocrAltoFile = new File(ocrAltoDir, page.getPosition() + ".xml");
            ocrProvider.fetchOcr(page.getImageFile(), ocrTextFile, ocrAltoFile);
        }

        //convert each page to jp2k (user copy, archive copy)
        File jp2kUserCopyDir = new File(workingDirBlock, "jp2k-usercopy");
        makeSureReadableWritableDirExists(jp2kUserCopyDir);
        File jp2kArchiveCopyDir = new File(workingDirBlock, "jp2k-archivecopy");
        makeSureReadableWritableDirExists(jp2kArchiveCopyDir);
        for (NamedPage page : pages) {
            File jp2kUserCopyFile = new File(jp2kUserCopyDir, page.getPosition() + ".jp2");
            File jp2kArchiveCopyFile = new File(jp2kArchiveCopyDir, page.getPosition() + ".jp2");
            jp2kConvertor.convertToJp2k(page.getImageFile(), jp2kUserCopyFile, jp2kArchiveCopyFile);
        }

        //marc xml
        Document marcXml = marcXmlProvider.getMarcXml(barcode.getValue());
        File marcXmlFile = new File(workingDirBlock, "marc.xml");
        Utils.saveDocumentToFile(marcXml, marcXmlFile);

        //marcxml to MODS
        Document modsDoc = marcToModsConvertor.convertMarcToMods(marcXml);
        File modsFile = new File(workingDirBlock, "mods.xml");
        Utils.saveDocumentToFile(modsDoc, modsFile);
        //System.out.println(Utils.prettyPrintDocument(modsDoc));
    }

    /**
     * Builds NDK package from data in blockWorkingDir into ndkPackageWorkingDir
     */
    private void processBlockPhase3(List<NamedPage> pages, Barcode barcode, File blockWorkingDir, File ndkPackageWorkingDir) {
        System.out.println("PHASE 3");
        makeSureReadableWritableDirExists(ndkPackageWorkingDir);
        System.out.println("TODO: create NDK package");
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
