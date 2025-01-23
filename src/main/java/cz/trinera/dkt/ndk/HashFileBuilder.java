package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HashFileBuilder {
    public void buildAndSave(File ndkPackageDir, Set<String> filePaths, File outFile) {
        List<String> lines = new ArrayList<>();
        filePaths.stream()
                .sorted((o1, o2) -> o1.length() == o2.length() ? o1.compareTo(o2) : Integer.compare(o1.length(), o2.length()))
                .filter(filePath ->
                        !(filePath.matches("/info.*\\.xml")) //exclude info file
                                && !(filePath.matches("/md5.*\\.md5")) //exclude md5 file

                )
                .forEach(filePath -> lines.add(processFilePath(filePath, ndkPackageDir)));
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

    private String processFilePath(String filePath, File ndkPackageDir) {
        String hash = Utils.computeMD5Checksum(new File(ndkPackageDir, filePath));
        return hash + "  " + filePath;
    }
}
