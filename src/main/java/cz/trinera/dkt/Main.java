package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorImplPyzbar;
import cz.trinera.dkt.tif2jp2.TifToJp2Converter;
import cz.trinera.dkt.tif2jp2.TifToJp2ConverterImplKakadu;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderImplYazClient;
import cz.trinera.dkt.marc2mods.MarcToModsConverter;
import cz.trinera.dkt.marc2mods.MarcToModsConverterImpl;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.ocr.OcrProviderMock;
import cz.trinera.dkt.tif2png.TifToPngConverter;
import cz.trinera.dkt.tif2png.TifToPngConverterImpl;
import cz.trinera.dkt.tif2png.TifToPngConverterMock;
import org.apache.commons.cli.*;

import java.io.File;

public class Main {

    private static final String OPT_CONFIG_FILE = "config_file";
    private static final String OPT_INPUT_DIR = "input_dir";
    private static final String OPT_OUTPUT_DIR = "output_dir";

    private static final boolean DEV_MODE = true;

    public static void main(String[] args) throws ToolAvailabilityError {
        if (DEV_MODE) {
            File homeDir = new File(System.getProperty("user.home"));

            //config
            File configFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/config.properties");

            //SAMPLE1: 0001.tif - 0027.tif (3 packages), ~/TrineraProjects/KramarskeTisky/data/input/sample1
            File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/input");
            File tmpDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/tmp");

            File _pngInputDir = new File(tmpDir, "_png-input");
            File _workingDir = new File(tmpDir, "_working");
            File _ndkPackageWorkingDir = new File(tmpDir, "_ndk-package");

            File resultsDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/results");
            run(configFile, inputDir, _pngInputDir, _workingDir, _ndkPackageWorkingDir, resultsDir);
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

            //TODO: option "cleanup" for cleaning temporary files afterwards (default true)

            // Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;

            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("java -jar DktCliApp.jar", options);
                System.exit(1);
                return;
            }

            // Retrieve the values of the options
            String configFilePath = cmd.getOptionValue(OPT_CONFIG_FILE);
            String inputDirectoryPath = cmd.getOptionValue(OPT_INPUT_DIR);
            String resultsDirectoryPath = cmd.getOptionValue(OPT_OUTPUT_DIR);

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
            File resultsDir = new File(resultsDirectoryPath);

            run(configFile, inputDir, _pngInputDir, _workingDir, _ndkPackageWorkingDir, resultsDir);
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

    private static DigitizationWorkflow getDigitizationWorkflow() throws ToolAvailabilityError {
        TifToPngConverter tifToPngConverter = new TifToPngConverterImpl(
                Config.instanceOf().getTifToPngConverterDependencyCheckScript(),
                Config.instanceOf().getTifToPngConverterScript()
        );
        if (DEV_MODE) {
            tifToPngConverter = new TifToPngConverterMock();
        }

        //BarcodeDetector barcodeDetector = new BarcodeDetectorPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");
        BarcodeDetector barcodeDetector = new BarcodeDetectorImplPyzbar(
                Config.instanceOf().getBarcodeDetectorPythonDependencyCheckScript(),
                Config.instanceOf().getBarcodeDetectorPythonScript()
        );

        OcrProvider ocrProvider = new OcrProviderImpl(
                Config.instanceOf().getOcrProviderPeroBaseUrl(),
                Config.instanceOf().getOcrProviderPeroApiKey(),
                Config.instanceOf().getOcrProviderPeroEngineId()
        );
        if (DEV_MODE) {
            ocrProvider = new OcrProviderMock();
        }

        //TifToJp2Converter tifToJp2Converter = new TifToJp2ConverterMock();
        TifToJp2Converter tifToJp2Converter = new TifToJp2ConverterImplKakadu(//TODO: from config
                "/Users/martinrehanek/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/tif2jp2/check_kakadu.py",
                "/Users/martinrehanek/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/tif2jp2/convert_tif_to_jp2_mc_uc.sh"
        );

        //MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock();
        MarcXmlProvider marcXmlProvider = new MarcXmlProviderImplYazClient(
                Config.instanceOf().getMarcXmlProviderPythonDependencyCheckScript(),
                Config.instanceOf().getMarcXmlProviderPythonScript(),
                Config.instanceOf().getMarcXmlProviderHost(),
                Config.instanceOf().getMarcXmlProviderPort(),
                Config.instanceOf().getMarcXmlProviderBase()
        );

        //MarcToModsConverter marcToModsConverter = new MarcToModsConverterImpl(marcToModsXsltFile.getAbsolutePath());
        MarcToModsConverter marcToModsConverter = new MarcToModsConverterImpl(
                Config.instanceOf().getMarcxmlToModsConverterXsltFile()
        );

        //check availability of all components
        tifToPngConverter.checkAvailable();
        barcodeDetector.checkAvailable();
        ocrProvider.checkAvailable();
        tifToJp2Converter.checkAvailable();
        marcXmlProvider.checkAvailable();
        marcToModsConverter.checkAvailable();

        return new DigitizationWorkflow(tifToPngConverter, barcodeDetector, ocrProvider, tifToJp2Converter, marcXmlProvider, marcToModsConverter);
    }
}