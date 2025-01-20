package cz.trinera.dkt.marc21;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarcXmlProviderImplYazClient implements MarcXmlProvider {

    private final String pythonExecutable;
    private final String pythonDependencyCheckScript;
    private final String pythonMarc21ByBarcodeScript;

    private final String host;
    private final int port;
    private final String base;

    private final Marc21ToMarcXmlConverter marc21ToMarcXmlConverter = new Marc21ToMarcXmlConverter();

    public MarcXmlProviderImplYazClient(String pythonDependencyCheckScript, String pythonMarc21ByBarcodeScript, String host, int port, String base) {
        this.pythonExecutable = Config.instanceOf().getPythonExecutable();
        this.pythonDependencyCheckScript = pythonDependencyCheckScript;
        this.pythonMarc21ByBarcodeScript = pythonMarc21ByBarcodeScript;
        this.host = host;
        this.port = port;
        this.base = base;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonDependencyCheckScriptFile = new File(pythonDependencyCheckScript);
        if (!pythonDependencyCheckScriptFile.exists()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " does not exist.");
        }
        if (!pythonDependencyCheckScriptFile.canRead()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " is not readable.");
        }
        File pythonBarcodeDetectionScriptFile = new File(pythonMarc21ByBarcodeScript);
        if (!pythonBarcodeDetectionScriptFile.exists()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonMarc21ByBarcodeScript + " does not exist.");
        }
        if (!pythonBarcodeDetectionScriptFile.canRead()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonMarc21ByBarcodeScript + " is not readable.");
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
                    if (line.startsWith("yaz-client is not available")) {
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                    if (line.startsWith("zbar is not available")) {
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                    if (line.startsWith("pillow is not available")) {
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " failed with output: " + line);
                    }
                } else {
                    throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " failed with empty output");
                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonDependencyCheckScript + " failed", e);
        }
    }

    @Override
    public Document getMarcXml(String barcode) {
        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(pythonMarc21ByBarcodeScript);
            command.add("--host");
            command.add(host);
            command.add("--port");
            command.add(String.valueOf(port));
            command.add("--base");
            command.add(base);
            command.add("--barcode");
            command.add(barcode);
            //command.add("--debug");

            //print command as string
            String commandStr = Arrays
                    .toString(command.toArray())
                    .substring(1, Arrays.toString(command.toArray()).length() - 1) // remove brackets
                    .replaceAll(", ", " "); // remove commas
            //System.out.println("command: " + commandStr);

            // Start the process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Redirect error stream to input stream
            processBuilder.environment().put("PATH", System.getenv("PATH"));
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

            // Convert marc21 to marcxml
            String result = output.toString();
            //System.out.println("result: ");
            //System.out.println(result);

            return marc21ToMarcXmlConverter.convert(result);
        } catch (Exception e) {
            throw new RuntimeException("MarcXml provider: Error while executing Python script " + pythonMarc21ByBarcodeScript, e);
        }
    }
}
