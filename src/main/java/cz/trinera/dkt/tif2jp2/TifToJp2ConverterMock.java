package cz.trinera.dkt.tif2jp2;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;
import java.io.IOException;

public class TifToJp2ConverterMock implements TifToJp2Converter {

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //do nothing in mock implementation
    }

    @Override
    public void convertToJp2(File inTifFile, File outArchivecopyJp2File, File outUsercopyJp2File) {
        System.out.println("Converting to jp2 images from " + inTifFile.getName());
        try {
            outArchivecopyJp2File.createNewFile();
            outUsercopyJp2File.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
