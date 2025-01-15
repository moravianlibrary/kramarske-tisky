package cz.trinera.dkt.tif2jp2;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TifToJp2ConverterImplKakadu implements TifToJp2Converter {

    private final String pythonExecutable;
    private final String pythonDependencyCheckScript;
    private final String conversionBashScript;

    public TifToJp2ConverterImplKakadu(String pythonDependencyCheckScript, String conversionBashScript) {
        this.pythonExecutable = Config.instanceOf().getPythonExecutable();
        this.pythonDependencyCheckScript = pythonDependencyCheckScript;
        this.conversionBashScript = conversionBashScript;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonDependencyCheckScriptFile = new File(pythonDependencyCheckScript);
        if (!pythonDependencyCheckScriptFile.exists()) {
            throw new ToolAvailabilityError("TifToJp2 converter: Python script " + pythonDependencyCheckScript + " does not exist.");
        }
        if (!pythonDependencyCheckScriptFile.canRead()) {
            throw new ToolAvailabilityError("TifToJp2 converter: Python script " + pythonDependencyCheckScript + " is not readable.");
        }
        File conversionBashScriptFile = new File(conversionBashScript);
        if (!conversionBashScriptFile.exists()) {
            throw new ToolAvailabilityError("TifToJp2 converter: Bash script " + conversionBashScript + " does not exist.");
        }
        if (!conversionBashScriptFile.canRead()) {
            throw new ToolAvailabilityError("TifToJp2 converter: Bash script " + conversionBashScript + " is not readable.");
        }
        if (!conversionBashScriptFile.canExecute()) {
            throw new ToolAvailabilityError("TifToJp2 converter: Bash script " + conversionBashScript + " is not executable.");
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
                    throw new ToolAvailabilityError("TifToJp2 converter: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                } else {
                    throw new ToolAvailabilityError("TifToJp2 converter: Python script " + pythonDependencyCheckScript + " failed with empty output");
                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("TifToJp2 converter: Python script " + pythonDependencyCheckScript + " failed", e);
        }
    }

    @Override
    public void convertToJp2(File inTifFile, File outArchivecopyJp2File, File outUsercopyJp2File) {
        System.out.println("Converting to jp2 (ac, mc) image " + inTifFile.getName());
        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add(conversionBashScript);
            command.add(inTifFile.getAbsolutePath());
            command.add(outArchivecopyJp2File.getAbsolutePath());
            command.add(outUsercopyJp2File.getAbsolutePath());

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
                throw new RuntimeException("TifToJp2 converter: Script " + conversionBashScript + " failed with exit code " + exitCode + ": " + output);
            }
        } catch (Exception e) {
            throw new RuntimeException("TifToJp2 converter: Error while executing script " + conversionBashScript, e);
        }
    }
}
