package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorImplPyzbar;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderImplYazClient;
import cz.trinera.dkt.marc21.MarcXmlProviderMock;
import cz.trinera.dkt.mods2dc.ModsToDcConverter;
import cz.trinera.dkt.mods2dc.ModsToDcConverterImpl;
import cz.trinera.dkt.marc2mods.MarcToModsConverter;
import cz.trinera.dkt.marc2mods.MarcToModsConverterImpl;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import cz.trinera.dkt.tif2jp2.TifToJp2Converter;
import cz.trinera.dkt.tif2jp2.TifToJp2ConverterImplKakadu;
import cz.trinera.dkt.tif2jp2.TifToJp2ConverterMock;
import cz.trinera.dkt.tif2png.TifToPngConverter;
import cz.trinera.dkt.tif2png.TifToPngConverterImpl;
import cz.trinera.dkt.tif2png.TifToPngConverterMock;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Map;

public class Main {

    private static final String OPT_TEST_DEPENDENCIES = "test_dependencies";
    private static final String OPT_CONFIG_FILE = "config_file";
    private static final String OPT_INPUT_DIR = "input_dir";
    private static final String OPT_OUTPUT_DIR = "output_dir";

    private static final boolean DEV_MODE = false;

    public static void main(String[] args) throws ToolAvailabilityError {
        if (DEV_MODE) {
            File homeDir = new File(System.getProperty("user.home"));

            //config
            File configFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/config.properties");

            //SAMPLE1: 0001.tif - 0027.tif (3 packages), ~/TrineraProjects/KramarskeTisky/data/input/sample3
            File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample3/input");
            File tmpDir = new File(inputDir, "tmp");

            File _pngInputDir = new File(tmpDir, "_png-input");
            File _workingDir = new File(tmpDir, "_working");
            File _ndkPackageWorkingDir = new File(tmpDir, "_ndk-package");

            File resultsDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample3/results");
            run(configFile, inputDir, _pngInputDir, _workingDir, _ndkPackageWorkingDir, resultsDir);

            //cleanup
            _workingDir.delete();
            _ndkPackageWorkingDir.delete();
            //_pngInputDir.delete(); //TODO: once original tiffs are moved
            //tmpDir.delete(); //TODO: once original tiffs are moved
        } else {
            // Create Options object
            Options options = new Options();

            // Define mandatory 'config' option
            Option optConfig = new Option("c", OPT_CONFIG_FILE, true, "Soubor s konfigurací (config.properties)");
            optConfig.setRequired(true);
            options.addOption(optConfig);

            // Define mandatory 'inputDir' option
            Option optInputDir = new Option("i", OPT_INPUT_DIR, true, "Vstupní adresář s TIFF soubory");
            optInputDir.setRequired(true);
            options.addOption(optInputDir);

            // Define mandatory 'resultsDir' option
            Option optResultsDir = new Option("o", OPT_OUTPUT_DIR, true, "Adresář pro ukládání výsledných balíčků pro import do Krameria");
            optResultsDir.setRequired(true);
            options.addOption(optResultsDir);

            //Define optional 'test_dependencies' option
            Option optTestDependencies = new Option("t", OPT_TEST_DEPENDENCIES, false, "Test dostupnosti nástrojů");
            optTestDependencies.setRequired(false);
            options.addOption(optTestDependencies);

            // Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;

            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("java -jar dkt-workflow-VERSION.jar", options);
                System.exit(1);
                return;
            }

            // Retrieve the values of the options
            String configFilePath = cmd.getOptionValue(OPT_CONFIG_FILE);
            String inputDirectoryPath = cmd.getOptionValue(OPT_INPUT_DIR);
            String outputDirectoryPath = cmd.getOptionValue(OPT_OUTPUT_DIR);

            //config file
            File configFile = new File(configFilePath);
            //input directory
            File inputDir = new File(inputDirectoryPath);
            //temporary directories
            File tmpDir = new File(inputDir, "tmp");
            File _pngInputDir = new File(tmpDir, "_png-input");
            File _workingDir = new File(tmpDir, "_working");
            File _ndkPackageWorkingDir = new File(tmpDir, "_ndk-package");
            //results directory
            File resultsDir = new File(outputDirectoryPath);

            if (cmd.hasOption(OPT_TEST_DEPENDENCIES)) {
                testDependencies(configFile);
            } else {
                run(configFile, inputDir, _pngInputDir, _workingDir, _ndkPackageWorkingDir, resultsDir);
                _workingDir.delete();
                _ndkPackageWorkingDir.delete();
                //_pngInputDir.delete();//TODO: once original tiffs are moved
                //tmpDir.delete(); //TODO: once original tiffs are moved
            }
        }
    }

    private static void testDependencies(File configFile) {
        try {
            //init configuration
            Config.init(configFile);

            DigitizationWorkflow digitizationWorkflow = getDigitizationWorkflow();
            Map<String, ToolAvailabilityError> errors = digitizationWorkflow.checkAvailabilitiesReturningErrors();
            System.out.println();
            System.out.println("Testing dependencies");
            System.out.println("--------------------");
            for (String errorKey : errors.keySet()) {
                ToolAvailabilityError error = errors.get(errorKey);
                System.out.println(errorKey + " " + (error == null ? "✅" : "❌"));
            }
            System.out.println();
            for (String errorKey : errors.keySet()) {
                ToolAvailabilityError error = errors.get(errorKey);
                if (error != null) {
                    error.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void run(File configFile, File inputDir, File pngInputDir, File workingDir, File ndkPackageWorkingDir, File resultsDir) throws ToolAvailabilityError {
        try {
            //init configuration
            Config.init(configFile);
            //System.out.println(Config.instanceOf());

            System.out.println("Preparing digitization workflow");
            System.out.println("Input dir: " + inputDir.getAbsolutePath());
            System.out.println("Working dir: " + workingDir.getAbsolutePath());
            System.out.println("NDK package working dir: " + ndkPackageWorkingDir.getAbsolutePath());
            System.out.println("Kramerius import dir: " + resultsDir.getAbsolutePath());
            System.out.println();

            DigitizationWorkflow digitizationWorkflow = getDigitizationWorkflow();
            System.out.println("Checking availabilities of all dependencies");
            digitizationWorkflow.checkAvailabilitiesThrowingException();
            System.out.println("Running digitization workflow");
            digitizationWorkflow.run(inputDir, pngInputDir, workingDir, ndkPackageWorkingDir, resultsDir);
        } catch (ToolAvailabilityError e) {
            System.err.println("Availability error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static DigitizationWorkflow getDigitizationWorkflow() {
        //TIFF to PNG converter
        TifToPngConverter tifToPngConverter = new TifToPngConverterImpl(
                Config.instanceOf().getTifToPngConverterDependencyCheckScript(),
                Config.instanceOf().getTifToPngConverterScript()
        );
        if (DEV_MODE) {
            tifToPngConverter = new TifToPngConverterMock();
        }

        //Barcode detector
        //BarcodeDetector barcodeDetector = new BarcodeDetectorPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");
        BarcodeDetector barcodeDetector = new BarcodeDetectorImplPyzbar(
                Config.instanceOf().getBarcodeDetectorPythonDependencyCheckScript(),
                Config.instanceOf().getBarcodeDetectorPythonScript()
        );
        if (DEV_MODE) {
            barcodeDetector = new BarcodeDetectorMock();
        }

        //OCR provider
        OcrProvider ocrProvider = new OcrProviderImpl(
                Config.instanceOf().getOcrProviderPeroBaseUrl(),
                Config.instanceOf().getOcrProviderPeroApiKey(),
                Config.instanceOf().getOcrProviderPeroEngineId()
        );
        if (DEV_MODE) {
            ocrProvider = new OcrProviderMock();
        }

        //TIFF to JP2 converter
        TifToJp2Converter tifToJp2Converter = new TifToJp2ConverterImplKakadu(
                Config.instanceOf().getTifToJp2ConverterDependencyCheckScript(),
                Config.instanceOf().getTifToJp2ConverterScript()
        );
        if (DEV_MODE) {
            tifToJp2Converter = new TifToJp2ConverterMock();
        }

        //MARC XML provider
        MarcXmlProvider marcXmlProvider = new MarcXmlProviderImplYazClient(
                Config.instanceOf().getMarcXmlProviderPythonDependencyCheckScript(),
                Config.instanceOf().getMarcXmlProviderPythonScript(),
                Config.instanceOf().getMarcXmlProviderHost(),
                Config.instanceOf().getMarcXmlProviderPort(),
                Config.instanceOf().getMarcXmlProviderBase()
        );
        if (DEV_MODE) {
            marcXmlProvider = new MarcXmlProviderMock();
        }

        MarcToModsConverter marcToModsConverter = new MarcToModsConverterImpl(Config.instanceOf().getMarcxmlToModsConverterXsltFile());
        ModsToDcConverter modsToDcConverter = new ModsToDcConverterImpl(Config.instanceOf().getModsToDcConverterXsltFile());

        return new DigitizationWorkflow(tifToPngConverter, barcodeDetector, ocrProvider, tifToJp2Converter, marcXmlProvider, marcToModsConverter, modsToDcConverter);
    }
}