package cz.trinera.dkt.jp2k;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;
import java.io.IOException;

public class Jp2KConverterMock implements Jp2kConverter {

    @Override
    public void convertToJp2k(File inPngFile, File outUsercopyJp2kFile, File outArchivecopyJp2kFile) {
        System.out.println("Converting to jp2k image " + inPngFile.getName());
        try {
            outUsercopyJp2kFile.createNewFile();
            outArchivecopyJp2kFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //do nothing in mock implementation
    }
}
