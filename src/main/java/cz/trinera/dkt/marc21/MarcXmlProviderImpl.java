package cz.trinera.dkt.marc21;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MarcXmlProviderImpl implements MarcXmlProvider {

    private final String pythonExecutable;
    private final String pythonCheckYazClientScript;
    private final String pythonMarc21ByBarcodeScript;

    private final String host;
    private final int port;
    private final String base;

    private final Marc21ToMarcXmlConvertor marc21ToMarcXmlConvertor = new Marc21ToMarcXmlConvertor();

    public MarcXmlProviderImpl(String pythonCheckYazClientScript, String pythonMarc21ByBarcodeScript, String host, int port, String base) {
        this.pythonExecutable = Config.instanceOf().getPythonExecutable();
        this.pythonCheckYazClientScript = pythonCheckYazClientScript;
        this.pythonMarc21ByBarcodeScript = pythonMarc21ByBarcodeScript;
        this.host = host;
        this.port = port;
        this.base = base;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        File pythonLibrariesCheckScriptFile = new File(pythonCheckYazClientScript);
        if (!pythonLibrariesCheckScriptFile.exists()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " does not exist.");
        }
        if (!pythonLibrariesCheckScriptFile.canRead()) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " is not readable.");
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
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, pythonCheckYazClientScript);
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
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " failed with output: " + line);
                    }
                    if (line.startsWith("zbar is not available")) {
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " failed with output: " + line);
                    }
                    if (line.startsWith("pillow is not available")) {
                        throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " failed with output: " + line);
                    }
                } else {
                    throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " failed with empty output");
                }
            }
        } catch (ToolAvailabilityError e) {
            throw e;
        } catch (Exception e) {
            throw new ToolAvailabilityError("MarcXml provider: Python script " + pythonCheckYazClientScript + " failed", e);
        }
    }

    @Override
    public Document getMarcXml(String barcode) {
        try {
            // Build the command to run the Python script
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(pythonMarc21ByBarcodeScript);
            command.add("--hos");
            command.add(host);
            command.add("--port");
            command.add(String.valueOf(port));
            command.add("--base");
            command.add(base);
            command.add("--barcode");
            command.add(barcode);

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

            // Convert marc21 to marcxml
            String result = output.toString();
            //System.out.println("result: ");
            //System.out.println(result);

            return marc21ToMarcXmlConvertor.convert(result);

        } catch (Exception e) {
            throw new RuntimeException("MarcXml provider: Error while executing Python script " + pythonMarc21ByBarcodeScript, e);
        }
    }
}
