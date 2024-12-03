package cz.trinera.dkt.barcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BarcodeDetectorPyzbar implements BarcodeDetector {

    private final String pythonScriptPath;

    public BarcodeDetectorPyzbar(String pythonScriptPath) {
        this.pythonScriptPath = pythonScriptPath;
    }

    @Override
    public Barcode detect(File imagePngFile) {
        if (imagePngFile == null || !imagePngFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + imagePngFile);
        }

        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add(pythonScriptPath);
            command.add(imagePngFile.getAbsolutePath());

            // Start the process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Redirect error stream to input stream
            Process process = processBuilder.start();

            // Capture the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script failed with exit code " + exitCode + ": " + output);
            }

            // Parse the output to find the barcode (you can customize this logic)
            String result = output.toString();
            if (result.contains("Detected Code39 Barcode:")) {
                String barcodeValue = result.split("Detected Code39 Barcode:")[1].trim();
                // Create Barcode object with both format and value
                return new Barcode("CODE39", barcodeValue); // Assuming Barcode format is Code39
            } else {
                // Optionally, check for other barcode formats here (e.g., Code128, EAN13, etc.)
                // If no barcode detected or any other barcode type, handle accordingly
                throw new RuntimeException("No barcode detected: " + result);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while executing Python script", e);
        }
    }
}
