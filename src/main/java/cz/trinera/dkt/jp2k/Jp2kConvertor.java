package cz.trinera.dkt.jp2k;

import cz.trinera.dkt.AvailabilityError;

import java.io.File;

public interface Jp2kConvertor {

    public void convertToJp2k(File inPngFile, File outUsercopyJp2kFile, File outArchivecopyJp2kFile);

    public void checkAvailable() throws AvailabilityError;
}
