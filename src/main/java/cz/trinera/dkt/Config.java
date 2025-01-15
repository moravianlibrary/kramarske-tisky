package cz.trinera.dkt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance;

    //prod configuration properties
    private final String pythonExecutable;
    private final String barcodeDetectorPythonDependencyCheckScript;
    private final String barcodeDetectorPythonScript;
    private final String tifToPngConverterDependencyCheckScript;
    private final String tifToPngConverterScript;
    private final String tifToJp2ConverterDependencyCheckScript;
    private final String tifToJp2ConverterScript;
    private final String marcxmlToModsConverterXsltFile;
    private final String ocrProviderPeroBaseUrl;
    private final String ocrProviderPeroApiKey;
    private final int ocrProviderPeroEngineId;
    private final String marcXmlProviderPythonDependencyCheckScript;
    private final String marcXmlProviderPythonScript;
    private final String marcXmlProviderHost;
    private final int marcXmlProviderPort;
    private final String marcXmlProviderBase;

    //dev configuration properties
    private final boolean devDisableTifToPngConversion;
    private final Integer devMaxBlocksToProcess;


    public static void init(File propertiesFile) throws IOException {
        instance = new Config(propertiesFile);
    }

    public static Config instanceOf() {
        if (instance == null) {
            throw new RuntimeException("Config not initialized");
        }
        return instance;
    }

    private Config(File propertiesFile) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        pythonExecutable = properties.getProperty("python_executable");
        if (properties.containsKey("dev.disable_tif_to_png_conversion")) {
            devDisableTifToPngConversion = Boolean.parseBoolean(properties.getProperty("dev.disable_tif_to_png_conversion"));
        } else {
            devDisableTifToPngConversion = false;
        }
        if (properties.containsKey("dev.max_blocks_to_process")) {
            devMaxBlocksToProcess = Integer.parseInt(properties.getProperty("dev.max_blocks_to_process"));
        } else {
            devMaxBlocksToProcess = null;
        }
        barcodeDetectorPythonDependencyCheckScript = getNonemptyProperty(properties, "barcode_detector.python_dependency_check_script");
        barcodeDetectorPythonScript = getNonemptyProperty(properties, "barcode_detector.python_script");
        tifToPngConverterDependencyCheckScript = getNonemptyProperty(properties, "tif_to_png_converter.dependency_check_script");
        tifToPngConverterScript = getNonemptyProperty(properties, "tif_to_png_converter.script");
        tifToJp2ConverterDependencyCheckScript = getNonemptyProperty(properties, "tif_to_jp2_converter.dependency_check_script");
        tifToJp2ConverterScript = getNonemptyProperty(properties, "tif_to_jp2_converter.script");
        marcxmlToModsConverterXsltFile = getNonemptyProperty(properties, "marcxml_to_mods_converter.xslt_file");
        ocrProviderPeroBaseUrl = getNonemptyProperty(properties, "ocr_provider_pero.base_url");
        ocrProviderPeroApiKey = getNonemptyProperty(properties, "ocr_provider_pero.api_key");
        ocrProviderPeroEngineId = Integer.parseInt(getNonemptyProperty(properties, "ocr_provider_pero.engine_id"));
        marcXmlProviderPythonDependencyCheckScript = getNonemptyProperty(properties, "marc_xml_provider.python_dependency_check_script");
        marcXmlProviderPythonScript = getNonemptyProperty(properties, "marc_xml_provider.python_script");
        marcXmlProviderHost = getNonemptyProperty(properties, "marc_xml_provider.host");
        marcXmlProviderPort = Integer.parseInt(getNonemptyProperty(properties, "marc_xml_provider.port"));
        marcXmlProviderBase = getNonemptyProperty(properties, "marc_xml_provider.base");
    }

    private String getNonemptyProperty(Properties properties, String key) {
        if (!properties.containsKey(key)) {
            throw new IllegalArgumentException("Missing property: " + key);
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing property: " + key);
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty property: " + key);
        }
        return value;
    }

    public boolean isDevTifToPngConversionDisabled() {
        return devDisableTifToPngConversion;
    }

    public Integer getDevMaxBlocksToProcess() {
        return devMaxBlocksToProcess;
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public String getBarcodeDetectorPythonDependencyCheckScript() {
        return barcodeDetectorPythonDependencyCheckScript;
    }

    public String getBarcodeDetectorPythonScript() {
        return barcodeDetectorPythonScript;
    }

    public String getTifToPngConverterDependencyCheckScript() {
        return tifToPngConverterDependencyCheckScript;
    }

    public String getTifToPngConverterScript() {
        return tifToPngConverterScript;
    }

    public String getTifToJp2ConverterDependencyCheckScript() {
        return tifToJp2ConverterDependencyCheckScript;
    }

    public String getTifToJp2ConverterScript() {
        return tifToJp2ConverterScript;
    }

    public String getMarcxmlToModsConverterXsltFile() {
        return marcxmlToModsConverterXsltFile;
    }

    public String getOcrProviderPeroBaseUrl() {
        return ocrProviderPeroBaseUrl;
    }

    public String getOcrProviderPeroApiKey() {
        return ocrProviderPeroApiKey;
    }

    public int getOcrProviderPeroEngineId() {
        return ocrProviderPeroEngineId;
    }

    public String getMarcXmlProviderPythonDependencyCheckScript() {
        return marcXmlProviderPythonDependencyCheckScript;
    }

    public String getMarcXmlProviderPythonScript() {
        return marcXmlProviderPythonScript;
    }

    public String getMarcXmlProviderHost() {
        return marcXmlProviderHost;
    }

    public int getMarcXmlProviderPort() {
        return marcXmlProviderPort;
    }

    public String getMarcXmlProviderBase() {
        return marcXmlProviderBase;
    }

    @Override
    public String toString() {
        return "Config{" +
                "pythonExecutable='" + pythonExecutable + '\'' +
                ", barcodeDetectorPythonDependencyCheckScript='" + barcodeDetectorPythonDependencyCheckScript + '\'' +
                ", barcodeDetectorPythonScript='" + barcodeDetectorPythonScript + '\'' +
                ", tifToPngConverterDependencyCheckScript='" + tifToPngConverterDependencyCheckScript + '\'' +
                ", tifToPngConverterScript='" + tifToPngConverterScript + '\'' +
                ", tifToJp2ConverterDependencyCheckScript='" + tifToJp2ConverterDependencyCheckScript + '\'' +
                ", tifToJp2ConverterScript='" + tifToJp2ConverterScript + '\'' +
                ", marcxmlToModsConverterXsltFile='" + marcxmlToModsConverterXsltFile + '\'' +
                ", ocrProviderPeroBaseUrl='" + ocrProviderPeroBaseUrl + '\'' +
                ", ocrProviderPeroEngieId=" + ocrProviderPeroEngineId + '\'' +
                ", marcXmlProviderPythonDependencyCheckScript='" + marcXmlProviderPythonDependencyCheckScript + '\'' +
                ", marcXmlProviderPythonScript='" + marcXmlProviderPythonScript + '\'' +
                ", marcXmlProviderHost='" + marcXmlProviderHost + '\'' +
                ", marcXmlProviderPort=" + marcXmlProviderPort + '\'' +
                ", marcXmlProviderBase='" + marcXmlProviderBase + '\'' +
                ", devDisableTifToPngConversion=" + devDisableTifToPngConversion +
                ", devMaxBlocksToProcess=" + devMaxBlocksToProcess +
                '}';
    }
}
