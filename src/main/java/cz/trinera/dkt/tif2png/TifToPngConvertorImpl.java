package cz.trinera.dkt.tif2png;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TifToPngConvertorImpl implements TifToPngConvertor {

    private final String dependencyChecScriptPath;
    private final String tifToPngScriptPath;

    public TifToPngConvertorImpl(String dependencyChecScriptPath, String tifToPngScriptPath) {
        this.dependencyChecScriptPath = dependencyChecScriptPath;
        this.tifToPngScriptPath = tifToPngScriptPath;
    }

    @Override
    public void convertAllTifFilesToPng(File inputDir, File outputDir) {
        //copy all tif files from inputDir to outputDir
        System.out.println("copying tif files from " + inputDir.getAbsolutePath() + " to " + outputDir.getAbsolutePath());
        File[] inputTifFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".tif"));
        for (File inputTifFile : inputTifFiles) {
            //convert tif file to png
            File outputTifFile = new File(outputDir, inputTifFile.getName());
            Utils.copyFile(inputTifFile, outputTifFile);
        }
        System.out.println("copied " + inputTifFiles.length + " tif files");

        //run conversion script over outputDir
        System.out.println("Running tif2png conversion script on " + outputDir.getAbsolutePath());
        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add(tifToPngScriptPath);
            command.add(outputDir.getAbsolutePath());

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
                throw new RuntimeException("Tif2Png convertor: Script " + tifToPngScriptPath + " failed with exit code " + exitCode + ": " + output);
            }
        } catch (Exception e) {
            throw new RuntimeException("Tif2Png convertor: Error while executing script " + tifToPngScriptPath, e);
        }
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File checkImageMagickSkript = new File(dependencyChecScriptPath);
        if (!checkImageMagickSkript.exists()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " does not exist.");
        }
        if (!checkImageMagickSkript.canRead()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " is not readable.");
        }
        if (!checkImageMagickSkript.canExecute()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " is not executable.");
        }
        File tifToPngScript = new File(tifToPngScriptPath);
        if (!tifToPngScript.exists()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + tifToPngScriptPath + " does not exist.");
        }
        if (!tifToPngScript.canRead()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + tifToPngScriptPath + " is not readable.");
        }
        if (!tifToPngScript.canExecute()) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + tifToPngScriptPath + " is not executable.");
        }
        // run the check script to verify the availability of the required Python packages
        try {
            // Command to run the Python script
            ProcessBuilder processBuilder = new ProcessBuilder(dependencyChecScriptPath);
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
                    throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " failed: " + line);
                } else {
                    throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " failed");

                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("Tif2Png detector: Script " + dependencyChecScriptPath + " failed", e);
        }
    }
}
