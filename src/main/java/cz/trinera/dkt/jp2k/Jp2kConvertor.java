package cz.trinera.dkt.jp2k;

import java.io.File;

public interface Jp2kConvertor {

    public void convertToJp2k(File inPngFile, File outUsercopyJp2kFile, File outArchivecopyJp2kFile);

}
