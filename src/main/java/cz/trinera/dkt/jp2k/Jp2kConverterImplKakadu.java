package cz.trinera.dkt.jp2k;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Jp2kConverterImplKakadu implements Jp2kConverter {

    private final String pythonExecutable;
    private final String pythonDependencyCheckScript;
    private final String conversionBashScript;

    public Jp2kConverterImplKakadu(String pythonDependencyCheckScript, String conversionBashScript) {
        this.pythonExecutable = Config.instanceOf().getPythonExecutable();
        this.pythonDependencyCheckScript = pythonDependencyCheckScript;
        this.conversionBashScript = conversionBashScript;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonDependencyCheckScriptFile = new File(pythonDependencyCheckScript);
        if (!pythonDependencyCheckScriptFile.exists()) {
            throw new ToolAvailabilityError("Jp2k converter: Python script " + pythonDependencyCheckScript + " does not exist.");
        }
        if (!pythonDependencyCheckScriptFile.canRead()) {
            throw new ToolAvailabilityError("Jp2k converter: Python script " + pythonDependencyCheckScript + " is not readable.");
        }
        File conversionBashScriptFile = new File(conversionBashScript);
        if (!conversionBashScriptFile.exists()) {
            throw new ToolAvailabilityError("Jp2k converter: Bash script " + conversionBashScript + " does not exist.");
        }
        if (!conversionBashScriptFile.canRead()) {
            throw new ToolAvailabilityError("Jp2k converter: Bash script " + conversionBashScript + " is not readable.");
        }
        if (!conversionBashScriptFile.canExecute()) {
            throw new ToolAvailabilityError("Jp2k converter: Bash script " + conversionBashScript + " is not executable.");
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
                    throw new ToolAvailabilityError("Jp2k converter: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                } else {
                    throw new ToolAvailabilityError("Jp2k converter: Python script " + pythonDependencyCheckScript + " failed with empty output");
                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("Jp2k converter: Python script " + pythonDependencyCheckScript + " failed", e);
        }
    }

    @Override
    public void convertToJp2k(File inPngFile, File outUsercopyJp2kFile, File outArchivecopyJp2kFile) {
        //TODO: implement properly
        System.out.println("Converting to jp2k images " + inPngFile.getName());
        try {
            outUsercopyJp2kFile.createNewFile();
            outArchivecopyJp2kFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
