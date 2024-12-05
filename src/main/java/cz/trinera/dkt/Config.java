package cz.trinera.dkt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance;

    //prod configuration properties
    private final String pythonExecutable;
    //dev configuration properties
    private final boolean disableTifToPngConversion;
    private final Integer maxBlocksToProcess;

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
        if (properties.containsKey("disable_tif_to_png_conversion")) {
            disableTifToPngConversion = Boolean.parseBoolean(properties.getProperty("disable_tif_to_png_conversion"));
        } else {
            disableTifToPngConversion = false;
        }
        if (properties.containsKey("max_blocks_to_process")) {
            maxBlocksToProcess = Integer.parseInt(properties.getProperty("max_blocks_to_process"));
        } else {
            maxBlocksToProcess = null;
        }
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public boolean isTifToPngConversionDisabled() {
        return disableTifToPngConversion;
    }

    public Integer getMaxBlocksToProcess() {
        return maxBlocksToProcess;
    }

    @Override
    public String toString() {
        return "Config{" +
                "pythonExecutable='" + pythonExecutable + '\'' +
                ", disableTifToPngConversion=" + disableTifToPngConversion +
                ", maxBlocksToProcess=" + maxBlocksToProcess +
                '}';
    }
}
