package cz.trinera.dkt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

    private static final int BLOCK_SIZE = 8192; // 8 KB

    public static void copyFile(File inFile, File outFile) {
        try (FileInputStream inputStream = new FileInputStream(inFile);
             FileOutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;

            // Read and write data in chunks
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error while copying file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
