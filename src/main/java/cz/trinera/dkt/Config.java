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
    private final String tifToPngConvertorDependencyCheckScript;
    private final String tifToPngConvertorScript;
    private final String marcxmlToModsConvertorXsltFile;
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
        barcodeDetectorPythonDependencyCheckScript = properties.getProperty("barcode_detector.python_dependency_check_script");
        barcodeDetectorPythonScript = properties.getProperty("barcode_detector.python_script");
        tifToPngConvertorDependencyCheckScript = properties.getProperty("tif_to_png_convertor.dependency_check_script");
        tifToPngConvertorScript = properties.getProperty("tif_to_png_convertor.script");
        marcxmlToModsConvertorXsltFile = properties.getProperty("marcxml_to_mods_convertor.xslt_file");
        ocrProviderPeroBaseUrl = properties.getProperty("ocr_provider_pero.base_url");
        ocrProviderPeroApiKey = properties.getProperty("ocr_provider_pero.api_key");
        ocrProviderPeroEngineId = Integer.parseInt(properties.getProperty("ocr_provider_pero.engine_id"));
        marcXmlProviderPythonDependencyCheckScript = properties.getProperty("marc_xml_provider.python_dependency_check_script");
        marcXmlProviderPythonScript = properties.getProperty("marc_xml_provider.python_script");
        marcXmlProviderHost = properties.getProperty("marc_xml_provider.host");
        marcXmlProviderPort = Integer.parseInt(properties.getProperty("marc_xml_provider.port"));
        marcXmlProviderBase = properties.getProperty("marc_xml_provider.base");
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

    public String getTifToPngConvertorDependencyCheckScript() {
        return tifToPngConvertorDependencyCheckScript;
    }

    public String getTifToPngConvertorScript() {
        return tifToPngConvertorScript;
    }

    public String getMarcxmlToModsConvertorXsltFile() {
        return marcxmlToModsConvertorXsltFile;
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
                ", tifToPngConvertorDependencyCheckScript='" + tifToPngConvertorDependencyCheckScript + '\'' +
                ", tifToPngConvertorScript='" + tifToPngConvertorScript + '\'' +
                ", marcxmlToModsConvertorXsltFile='" + marcxmlToModsConvertorXsltFile + '\'' +
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
