package cz.trinera.dkt.ndk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HashFileBuilder {
    public void buildAndSave(File ndkPackageDir, Set<FileInfo> fileInfos, File outFile) {
        List<String> lines = new ArrayList<>();
        fileInfos.stream()
                .sorted((o1, o2) -> o1.getPathFromNdkPackageRoot(false).length() == o2.getPathFromNdkPackageRoot(false).length()
                        ? o1.getPathFromNdkPackageRoot(false).compareTo(o2.getPathFromNdkPackageRoot(false))
                        : Integer.compare(o1.getPathFromNdkPackageRoot(false).length(), o2.getPathFromNdkPackageRoot(false).length()))
                .filter(fileInfo -> !(fileInfo.getFile().getName().matches("info_.*\\.xml"))) //exclude info file
                .filter(fileInfo -> !(fileInfo.getFile().getName().matches("md5_.*\\.md5"))) //exclude md5 file
                .forEach(filePath -> lines.add(filePath.getMd5Checksum() + " " + filePath.getPathFromNdkPackageRoot(true)));
        saveLinesToFile(outFile, lines);
    }

    private void saveLinesToFile(File outFile, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
