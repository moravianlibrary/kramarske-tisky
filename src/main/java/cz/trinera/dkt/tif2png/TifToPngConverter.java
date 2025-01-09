package cz.trinera.dkt.tif2png;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public interface TifToPngConverter {

    public void convertAllTifFilesToPng(File inputDir, File outputDir);

    public void checkAvailable() throws ToolAvailabilityError;

}
