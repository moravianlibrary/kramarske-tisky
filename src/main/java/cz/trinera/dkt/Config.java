package cz.trinera.dkt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance;

    //prod configuration properties
    private final String pythonExecutable;
    private final String barcodeDetectorPythonLibrariesCheckScript;
    private final String barcodeDetectorPythonScript;
    private final String tifToPngConvertorLibrariesCheckScript;
    private final String tifToPngConvertorScript;
    private final String marcxmlToModsConvertorXsltFile;
    private final String ocrProviderPeroBaseUrl;
    private final String ocrProviderPeroApiKey;
    private final int ocrProviderPeroEngineId;

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
        barcodeDetectorPythonLibrariesCheckScript = properties.getProperty("barcode_detector.python_libraries_check_script");
        barcodeDetectorPythonScript = properties.getProperty("barcode_detector.python_script");
        tifToPngConvertorLibrariesCheckScript = properties.getProperty("tif_to_png_convertor.libraries_check_script");
        tifToPngConvertorScript = properties.getProperty("tif_to_png_convertor.script");
        marcxmlToModsConvertorXsltFile = properties.getProperty("marcxml_to_mods_convertor.xslt_file");
        ocrProviderPeroBaseUrl = properties.getProperty("ocr_provider_pero.base_url");
        ocrProviderPeroApiKey = properties.getProperty("ocr_provider_pero.api_key");
        ocrProviderPeroEngineId = Integer.parseInt(properties.getProperty("ocr_provider_pero.engine_id"));
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

    public String getBarcodeDetectorPythonLibrariesCheckScript() {
        return barcodeDetectorPythonLibrariesCheckScript;
    }

    public String getBarcodeDetectorPythonScript() {
        return barcodeDetectorPythonScript;
    }

    public String getTifToPngConvertorLibrariesCheckScript() {
        return tifToPngConvertorLibrariesCheckScript;
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

    @Override
    public String toString() {
        return "Config{" +
                "pythonExecutable='" + pythonExecutable + '\'' +
                ", barcodeDetectorPythonLibrariesCheckScript='" + barcodeDetectorPythonLibrariesCheckScript + '\'' +
                ", barcodeDetectorPythonScript='" + barcodeDetectorPythonScript + '\'' +
                ", tifToPngConvertorLibrariesCheckScript='" + tifToPngConvertorLibrariesCheckScript + '\'' +
                ", tifToPngConvertorScript='" + tifToPngConvertorScript + '\'' +
                ", marcxmlToModsConvertorXsltFile='" + marcxmlToModsConvertorXsltFile + '\'' +
                ", ocrProviderPeroBaseUrl='" + ocrProviderPeroBaseUrl + '\'' +
                ", ocrProviderPeroEngieId=" + ocrProviderPeroEngineId + '\'' +
                ", devDisableTifToPngConversion=" + devDisableTifToPngConversion +
                ", devMaxBlocksToProcess=" + devMaxBlocksToProcess +
                '}';
    }
}
