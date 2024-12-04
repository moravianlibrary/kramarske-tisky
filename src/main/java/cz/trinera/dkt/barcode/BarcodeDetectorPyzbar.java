package cz.trinera.dkt.barcode;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BarcodeDetectorPyzbar implements BarcodeDetector {

    //TODO: make configurable
    //private final String pythonBinary = "python3";
    private final String pythonBinary = "src/main/resources/barcode/venv/bin/python3";

    private final String pythonCheckScriptPath;
    private final String pythonScriptPath;

    public BarcodeDetectorPyzbar(String pythonCheckScriptPath, String pythonScriptPath) {
        this.pythonCheckScriptPath = pythonCheckScriptPath;
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
            command.add(pythonBinary);
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

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonCheckScript = new File(pythonCheckScriptPath);
        if (!pythonCheckScript.exists()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " does not exist.");
        }
        if (!pythonCheckScript.canRead()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " is not readable.");
        }
        File pythonScript = new File(pythonScriptPath);
        if (!pythonScript.exists()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonScriptPath + " does not exist.");
        }
        if (!pythonScript.canRead()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonScriptPath + " is not readable.");
        }
        // run the check script to verify the availability of the required Python packages
        try {
            // Command to run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(pythonBinary, pythonCheckScriptPath);
            // Start the process
            Process process = processBuilder.start();
            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("exit code:  " + exitCode);
            if (exitCode != 0) {
                throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " failed with exit code " + exitCode);
            }
            // Read the output of the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if ((line = reader.readLine()) != null) {
                System.out.println("line: " + line);
                if (line.startsWith("pyzbar is not installed")) {
                    throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " failed with output: " + line);
                }
            } else {
                throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " failed with empty output");
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonCheckScriptPath + " failed", e);
        }
    }
}
