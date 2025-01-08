package cz.trinera.dkt.barcode;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BarcodeDetectorImplPyzbar implements BarcodeDetector {

    private final String pythonExecutable;
    private final String pythonDependencyCheckScript;
    private final String pythonBarcodeDetectionScript;

    public BarcodeDetectorImplPyzbar(String pythonDependencyCheckScript, String pythonBarcodeDetectionScript) {
        this.pythonExecutable = Config.instanceOf().getPythonExecutable();
        this.pythonDependencyCheckScript = pythonDependencyCheckScript;
        this.pythonBarcodeDetectionScript = pythonBarcodeDetectionScript;
    }

    @Override
    public Barcode detect(File pngFile) {
        if (pngFile == null || !pngFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist: " + pngFile);
        }

        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(pythonBarcodeDetectionScript);
            command.add(pngFile.getAbsolutePath());

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
                //throw new RuntimeException("No barcode detected: " + result);
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Barcode detector: Error while executing Python script " + pythonDependencyCheckScript, e);
        }
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonDependencyCheckScriptFile = new File(pythonDependencyCheckScript);
        if (!pythonDependencyCheckScriptFile.exists()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " does not exist.");
        }
        if (!pythonDependencyCheckScriptFile.canRead()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " is not readable.");
        }
        File pythonBarcodeDetectionScriptFile = new File(pythonBarcodeDetectionScript);
        if (!pythonBarcodeDetectionScriptFile.exists()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonBarcodeDetectionScript + " does not exist.");
        }
        if (!pythonBarcodeDetectionScriptFile.canRead()) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonBarcodeDetectionScript + " is not readable.");
        }
        // run the check script to verify the availability of the required Python packages
        try {
            // Command to run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, pythonDependencyCheckScript);
            // Start the process
            Process process = processBuilder.start();
            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read the output of the Python script
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                if ((line = reader.readLine()) != null) {
                    System.out.println("line: " + line);
                    if (line.startsWith("pyzbar is not available")) {
                        throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                    if (line.startsWith("zbar is not available")) {
                        throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                    if (line.startsWith("pillow is not available")) {
                        throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                } else {
                    throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " failed with empty output");
                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("Barcode detector: Python script " + pythonDependencyCheckScript + " failed", e);
        }
    }
}
