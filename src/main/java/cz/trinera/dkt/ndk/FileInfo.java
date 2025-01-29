package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;

import java.io.File;

public class FileInfo {

    public enum Category {
        MC, UC, TXT, ALTO, AMDSEC
    }

    private final File file;
    private final Category category;
    private final String pathFromNdkPackageRoot;
    private Long fileSizeBytes;
    private Long fileSizeKilobytes;
    private String md5Checksum;
    private Integer pageNumber;

    public FileInfo(File ndkPackageDir, String pathFromNdkPackageRoot) {
        this.file = new File(ndkPackageDir, pathFromNdkPackageRoot);
        this.pathFromNdkPackageRoot = normalizePathFromPackageRoot(pathFromNdkPackageRoot);
        this.category = extractCategory(this.pathFromNdkPackageRoot);
        this.pageNumber = extractPageNumber();
    }

    /**
     * /some/path -> some/path
     * \some\path -> some/path
     * some/path -> some/path
     * some\path -> some/path
     */
    private String normalizePathFromPackageRoot(String pathFromNdkPackageRoot) {
        String result = pathFromNdkPackageRoot.replaceAll("\\\\", "/");
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    private Category extractCategory(String pathFromNdkPackageRoot) {
        //split by "/"
        String[] pathTokens = pathFromNdkPackageRoot.split("/");
        String dir = null;
        if (pathTokens.length != 0) {
            if (!pathTokens[0].isBlank()) {
                dir = pathTokens[0];
            } else if (pathTokens.length > 1 && !pathTokens[1].isBlank()) {
                {
                    dir = pathTokens[1];
                }
            }
        }
        if (dir != null) {
            switch (dir) {
                case "mastercopy":
                    return Category.MC;
                case "usercopy":
                    return Category.UC;
                case "txt":
                    return Category.TXT;
                case "alto":
                    return Category.ALTO;
                case "amdsec":
                    return Category.AMDSEC;
                default:
                    //System.err.println("Unknown category: " + dir);
                    return null;
            }
        } else {
            return null;
        }
    }

    private Integer extractPageNumber() {
        //convert txt_mzk-0008rk_0001.txt, uc_mzk-0008rk_0001.txt, mc_mzk-0008rk_0001.txt, amdSec_0008rk_0001.xml to number 1
        String filename = file.getName();
        String withoutSuffix = filename.substring(0, filename.lastIndexOf('.'));
        String[] filenameTokens = withoutSuffix.split("_");
        try {
            return Integer.parseInt(filenameTokens[filenameTokens.length - 1]);
        } catch (NumberFormatException e) {
            //System.err.println("Cannot extract page number from: " + pathFromNdkPackageRoot);
            return null;
        }
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

    public String getPathFromNdkPackageRoot(boolean startWithPathSeparator) {
        return startWithPathSeparator ? "/" + pathFromNdkPackageRoot : pathFromNdkPackageRoot;
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

    public Category getCategory() {
        return category;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }
}
