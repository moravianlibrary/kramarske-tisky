package cz.trinera.dkt.tif2png;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public class TifToPngConvertorImpl implements TifToPngConvertor {

    private final String checkImageMagickScript;
    private final String tifToPngScript;


    public TifToPngConvertorImpl(String checkImageMagickScript, String tifToPngScript) {
        this.checkImageMagickScript = checkImageMagickScript;
        this.tifToPngScript = tifToPngScript;

    }

    @Override
    public void convertAllTifFilesToPng(File inputDir, File outputDir) {
        //TODO: implement
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //TODO: implement
        throw new RuntimeException("Not implemented yet");
    }
}
