package cz.trinera.dkt.marc21;

import cz.trinera.dkt.Config;
import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;
import org.yaz4j.Connection;
import org.yaz4j.PrefixQuery;
import org.yaz4j.Record;
import org.yaz4j.ResultSet;
import org.yaz4j.exception.ZoomException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class MarcXmlProviderImpl implements MarcXmlProvider {

    private final String pythonExecutable;
    private final String pythonCheckYazClientScript;
    private final String pythonMarc21ByBarcodeScript;

    private final String host;
    private final int port;
    private final String base;

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
        //TODO: replace with python script
        try (Connection con = new Connection(host, port)) {
            //con.setSyntax("usmarc");
            con.setSyntax("marc21");
            con.setDatabaseName(base); //TODO: check if this is correct
            con.connect();
            ResultSet set = con.search(new PrefixQuery("find @attr 1=1063 " + barcode));
            Record rec = set.getRecord(0);
            System.out.println(rec.render());
            //TODO: extract marc text from record
            //TODO: convert marc text to marc xml
            return null;
        } catch (ZoomException ze) {
            throw new RuntimeException(ze);
        }
    }
}
