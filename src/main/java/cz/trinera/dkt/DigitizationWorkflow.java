package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetector.Barcode;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc2mods.MarcToModsConverter;
import cz.trinera.dkt.ndk.*;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.tif2jp2.TifToJp2Converter;
import cz.trinera.dkt.tif2png.TifToPngConverter;
import nu.xom.Document;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class DigitizationWorkflow {

    private static class InputImage {
        final File pngFile;
        final File tifFile;

        InputImage(File pngFile, File tifFile) {
            this.pngFile = pngFile;
            this.tifFile = tifFile;
        }
    }

    private final TifToPngConverter tifToPngConverter;
    private final BarcodeDetector barcodeDetector;
    private final OcrProvider ocrProvider;
    private final TifToJp2Converter tifToJp2Converter;
    private final MarcXmlProvider marcXmlProvider;
    private final MarcToModsConverter marcToModsConverter;

    public DigitizationWorkflow(TifToPngConverter tifToPngConverter, BarcodeDetector barcodeDetector, OcrProvider ocrProvider, TifToJp2Converter tifToJp2Converter, MarcXmlProvider marcXmlProvider, MarcToModsConverter marcToModsConverter) {
        this.tifToPngConverter = tifToPngConverter;
        this.barcodeDetector = barcodeDetector;
        this.ocrProvider = ocrProvider;
        this.tifToJp2Converter = tifToJp2Converter;
        this.marcXmlProvider = marcXmlProvider;
        this.marcToModsConverter = marcToModsConverter;
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
        if (!Config.instanceOf().isDevTifToPngConversionDisabled()) {
            System.out.println("Converting all tif images to png in " + inputDir + " to " + pngInputDir);
            this.tifToPngConverter.convertAllTifFilesToPng(inputDir, pngInputDir);
            System.out.println("All tif images converted to png");
            System.out.println();
        }

        //process all png files in the directory pngInputDir
        Barcode lastBarcode = null;
        List<InputImage> imagesToBeProcessed = new ArrayList<>();

        int processedBlocks = 0;
        Integer maxBlocksToProcess = Config.instanceOf().getDevMaxBlocksToProcess();
        for (File pngFile : listImageFiles(pngInputDir)) {
            if (maxBlocksToProcess != null && processedBlocks >= maxBlocksToProcess) {
                System.out.println("Limit MAX_BLOCKS_TO_PROCESS reached (" + maxBlocksToProcess + " blocks), quitting now");
                return;
            }
            if (pngFile.getName().endsWith(".png")) {
                //System.out.println("Listing " + file.getName());
                BarcodeDetector.Barcode barcode = barcodeDetector.detect(pngFile);
                if (barcode != null) {
                    if (lastBarcode == null) {
                        lastBarcode = barcode;
                    } else {
                        //next barcode found - process the block of all images before that
                        if (maxBlocksToProcess == null || processedBlocks < maxBlocksToProcess) {
                            processBlock(imagesToBeProcessed, lastBarcode, workingDir, ndkPackageWorkingDir, resultsDir);
                            processedBlocks++;
                        }
                        //set new barcode as last and reset images to be processed
                        lastBarcode = barcode;
                        imagesToBeProcessed.clear();
                    }
                } else {
                    imagesToBeProcessed.add(new InputImage(pngFile, new File(inputDir, pngFile.getName().replace(".png", ".tif"))));
                }
            }
        }
        if (lastBarcode != null) { //process the last block
            if (maxBlocksToProcess == null || processedBlocks < maxBlocksToProcess) {
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


    private void processBlock(List<InputImage> imagesToBeProcessed, BarcodeDetector.Barcode barcode, File workingDir, File ndkPackageWorkingDir, File resultsDir) {
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
        //phase 2 - fetch OCR, fetch marc, convert to MODS, convert tif images to jp2
        processBlockPhase2(namedPages, barcode, workingDirForBlock);

        //phase 3 - build NDK package
        processBlockPhase3(namedPages, barcode, workingDirForBlock, ndkPackageWorkingDirForBlock, ts);

        //TODO: phase 4: copy result to resultsDir, cleanup
    }


    /**
     * Checks image files, if they are valid block of pages (4, 8, 16 pages), create
     */
    private List<NamedPage> processBlockPhase1(List<InputImage> imagesToBeProcessed, BarcodeDetector.Barcode barcode, File workingDirForBlock) {
        System.out.println("PHASE 1");
        makeSureReadableWritableDirExists(workingDirForBlock);
        String pngImageFilesStr = imagesToBeProcessed.stream().map((InputImage t) -> t.pngFile.getName()).reduce((a, b) -> a + ", " + b).orElse("");
        System.out.println("Processing " + imagesToBeProcessed.size() + " files with barcode " + barcode.getValue() + ": " + pngImageFilesStr);
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
            pages.add(new NamedPage(i + 1, pageName, imagesToBeProcessed.get(i).pngFile, imagesToBeProcessed.get(i).tifFile));
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
            Utils.copyFile(page.getPngImageFile(), pageDestFile);
            pagesInWorkingDir.add(page.withDifferentPngImageFile(pageDestFile));
        }
        return pagesInWorkingDir;
    }

    /*
     * Process images in working dir (copy of original images), fetch marc and convert to MODS, fetch OCR, convert images (original tif versions) to jp2
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
            ocrProvider.fetchOcr(page.getPngImageFile(), ocrTextFile, ocrAltoFile);
        }

        //convert each page to jp2 (archive copy, user copy)
        File jp2ArchiveCopyDir = new File(workingDirBlock, "jp2-archivecopy");
        makeSureReadableWritableDirExists(jp2ArchiveCopyDir);
        File jp2UserCopyDir = new File(workingDirBlock, "jp2-usercopy");
        makeSureReadableWritableDirExists(jp2UserCopyDir);
        for (NamedPage page : pages) {
            File jp2ArchiveCopyFile = new File(jp2ArchiveCopyDir, page.getPosition() + ".jp2");
            File jp2UserCopyFile = new File(jp2UserCopyDir, page.getPosition() + ".jp2");
            tifToJp2Converter.convertToJp2(page.getTifImageFile(), jp2ArchiveCopyFile, jp2UserCopyFile);
        }

        //marc xml
        Document marcXml = marcXmlProvider.getMarcXml(barcode.getValue());
        File marcXmlFile = new File(workingDirBlock, "marc.xml");
        Utils.saveDocumentToFile(marcXml, marcXmlFile);

        //marcxml to MODS
        Document modsDoc = marcToModsConverter.convertMarcToMods(marcXml);
        File modsFile = new File(workingDirBlock, "mods.xml");
        Utils.saveDocumentToFile(modsDoc, modsFile);
        //System.out.println(Utils.prettyPrintDocument(modsDoc));
    }

    /**
     * Builds NDK package from data in blockWorkingDir into ndkPackageWorkingDir
     */
    private void processBlockPhase3(List<NamedPage> pages, Barcode barcode, File blockWorkingDir, File ndkPackageWorkingDir, Timestamp now) {
        System.out.println("PHASE 3");
        try {
            makeSureReadableWritableDirExists(ndkPackageWorkingDir);
            UUID packageUuid = UUID.randomUUID();
            System.out.println("Creating NDK package " + packageUuid);
            File ndkPackageDir = new File(ndkPackageWorkingDir, packageUuid.toString());
            ndkPackageDir.mkdirs();

            //MASTERCOPY
            File masterCopyDir = new File(ndkPackageDir, "mastercopy");
            masterCopyDir.mkdirs();
            File masterCopyInputDir = new File(blockWorkingDir, "jp2-archivecopy");
            Arrays.stream(masterCopyInputDir.listFiles((dir, name) -> name.endsWith(".jp2"))).forEach(
                    file -> {
                        int pageNumber = Integer.valueOf(file.getName().split("\\.")[0]);
                        String newName = "mc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".jp2";
                        File newFile = new File(masterCopyDir, newName);
                        Utils.copyFile(file, newFile);
                    }
            );

            //USERCOPY (dir and files)
            File userCopyDir = new File(ndkPackageDir, "usercopy");
            userCopyDir.mkdirs();
            File userCopyInputDir = new File(blockWorkingDir, "jp2-usercopy");
            Arrays.stream(userCopyInputDir.listFiles((dir, name) -> name.endsWith(".jp2"))).forEach(
                    file -> {
                        int pageNumber = Integer.valueOf(file.getName().split("\\.")[0]);
                        String newName = "uc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".jp2";
                        File newFile = new File(userCopyDir, newName);
                        Utils.copyFile(file, newFile);
                    }
            );

            //ALTO (dir and files)
            File altoDir = new File(ndkPackageDir, "alto");
            altoDir.mkdirs();
            File altoInputDir = new File(blockWorkingDir, "ocr-alto");
            Arrays.stream(altoInputDir.listFiles((dir, name) -> name.endsWith(".xml"))).forEach(
                    file -> {
                        int pageNumber = Integer.valueOf(file.getName().split("\\.")[0]);
                        String newName = "alto_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".xml";
                        File newFile = new File(altoDir, newName);
                        Utils.copyFile(file, newFile);
                    }
            );

            //TXT (dir and files)
            File txtDir = new File(ndkPackageDir, "txt");
            txtDir.mkdirs();
            File txtInputDir = new File(blockWorkingDir, "ocr-text");
            Arrays.stream(txtInputDir.listFiles((dir, name) -> name.endsWith(".txt"))).forEach(
                    file -> {
                        int pageNumber = Integer.valueOf(file.getName().split("\\.")[0]);
                        String newName = "txt_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".txt";
                        File newFile = new File(txtDir, newName);
                        Utils.copyFile(file, newFile);
                    }
            );

            Set<FileInfo> fileInfos = new HashSet<>();
            fileInfos.add(new FileInfo(ndkPackageDir, "/info_" + packageUuid + ".xml"));
            fileInfos.add(new FileInfo(ndkPackageDir, "/mets_" + packageUuid + ".xml"));
            fileInfos.add(new FileInfo(ndkPackageDir, "/md5_" + packageUuid + ".md5"));
            Arrays.stream(masterCopyDir.listFiles()).forEach(file -> fileInfos.add(new FileInfo(ndkPackageDir, "/mastercopy/" + file.getName())));
            Arrays.stream(userCopyDir.listFiles()).forEach(file -> fileInfos.add(new FileInfo(ndkPackageDir, "/usercopy/" + file.getName())));
            Arrays.stream(altoDir.listFiles()).forEach(file -> fileInfos.add(new FileInfo(ndkPackageDir, "/alto/" + file.getName())));
            Arrays.stream(txtDir.listFiles()).forEach(file -> fileInfos.add(new FileInfo(ndkPackageDir, "/txt/" + file.getName())));
            String monographTitle = "TODO: nazev monografie";

            //SECONDARY METS (dir, files)
            File amdsecDir = new File(ndkPackageDir, "amdsec");
            amdsecDir.mkdirs();
            int pageCount = masterCopyDir.listFiles().length;
            SecMetsBuilder secMetsBuilder = new SecMetsBuilder(ndkPackageDir, packageUuid, now, fileInfos, monographTitle);
            for (int i = 1; i <= pageCount; i++) {
                secMetsBuilder.buildAndSavePage(i);
            }
            Arrays.stream(amdsecDir.listFiles()).forEach(file -> fileInfos.add(new FileInfo(ndkPackageDir, "/amdsec/" + file.getName())));

            //MAIN METS
            File mainMetsFile = new File(ndkPackageDir, "mets_" + packageUuid + ".xml");
            MainMetsBuilder mainMetsBuilder = new MainMetsBuilder(ndkPackageDir, packageUuid, now);
            Document mainMetsDoc = mainMetsBuilder.build(fileInfos, monographTitle, pages);
            Utils.saveDocumentToFile(mainMetsDoc, mainMetsFile);

            //MD5
            File md5File = new File(ndkPackageDir, "md5_" + packageUuid + ".md5");
            HashFileBuilder hashFileBuilder = new HashFileBuilder();
            hashFileBuilder.buildAndSave(ndkPackageDir, fileInfos, md5File);

            //INFO
            File infoXmlFile = new File(ndkPackageDir, "info_" + packageUuid + ".xml");
            InfoXmlBuilder infoXmlBuilder = new InfoXmlBuilder();
            Document infoXmlDoc = infoXmlBuilder.build(now, packageUuid, fileInfos, md5File);
            Utils.saveDocumentToFile(infoXmlDoc, infoXmlFile);
        } catch (Throwable e) {
            System.err.println("Error while creating NDK package: " + e.getMessage());
            e.printStackTrace();
        }
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

