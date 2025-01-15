package cz.trinera.dkt.tif2jp2;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public interface TifToJp2Converter {

    public void checkAvailable() throws ToolAvailabilityError;

    public void convertToJp2(File inTifFile, File outArchivecopyJp2File, File outUsercopyJp2File);

}
