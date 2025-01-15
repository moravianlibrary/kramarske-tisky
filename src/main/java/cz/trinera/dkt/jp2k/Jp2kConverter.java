package cz.trinera.dkt.jp2k;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public interface Jp2kConverter {

    public void checkAvailable() throws ToolAvailabilityError;

    public void convertToJp2k(File inPngFile, File outUsercopyJp2kFile, File outArchivecopyJp2kFile);

}
