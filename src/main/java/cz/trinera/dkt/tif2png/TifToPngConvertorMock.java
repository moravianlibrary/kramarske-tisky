package cz.trinera.dkt.tif2png;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;

import java.io.File;
import java.io.IOException;

public class TifToPngConvertorMock implements TifToPngConvertor {

    @Override
    public void convertAllTifFilesToPng(File inputDir, File outputDir) {
        System.out.println("creating empty png files from tif files in " + inputDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
        File[] inputTifFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".tif"));
        int counterCreated = 0;
        int counterFound = 0;
        for (File inputTifFile : inputTifFiles) {
            //convert tif file to png
            File outputPngFile = new File(outputDir, inputTifFile.getName().replace(".tif", ".png"));
            try {
                if (!outputPngFile.exists()) {
                    outputPngFile.createNewFile();
                    counterCreated++;
                } else {
                    counterFound++;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("created " + counterCreated + " empty png files (found " + counterFound + " existing png files)");
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //do nothing
    }
}
