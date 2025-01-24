package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;

import java.io.File;

public class FileInfo {

    private final File file;
    private final String pathFromNdkPackageRoot;
    private Long fileSizeBytes;
    private Long fileSizeKilobytes;
    private String md5Checksum;

    public FileInfo(File ndkPackageDir, String pathFromNdkPackageRoot) {
        this.file = new File(ndkPackageDir, pathFromNdkPackageRoot);
        this.pathFromNdkPackageRoot = pathFromNdkPackageRoot;
    }

    private String computeMd5Checksum(File file) {
        return Utils.computeMD5Checksum(file);
    }

    private long computeSize(File file) {
        long sizeBytes = file.length();
        long sizeKB = sizeBytes / 1024;
        return sizeKB;
    }

    public File getFile() {
        return file;
    }

    public String getPathFromNdkPackageRoot() {
        return pathFromNdkPackageRoot;
    }

    public Long getFileSizeBytes() {
        if (fileSizeBytes == null) {
            fileSizeBytes = file.length();
        }
        return fileSizeBytes;
    }

    public Long getFileSizeKilobytes() {
        if (fileSizeKilobytes == null) {
            fileSizeKilobytes = getFileSizeBytes() / 1024;
        }
        return fileSizeKilobytes;
    }

    public String getMd5Checksum() {
        if (md5Checksum == null) {
            md5Checksum = computeMd5Checksum(file);
        }
        return md5Checksum;
    }
}
