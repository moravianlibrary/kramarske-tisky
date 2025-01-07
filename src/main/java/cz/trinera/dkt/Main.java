package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorImplPyzbar;
import cz.trinera.dkt.jp2k.Jp2kConvertor;
import cz.trinera.dkt.jp2k.Jp2kConvertorMock;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderMock;
import cz.trinera.dkt.marc2mods.MarcToModsConvertor;
import cz.trinera.dkt.marc2mods.MarcToModsConvertorImpl;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderImpl;
import cz.trinera.dkt.tif2png.TifToPngConvertor;
import cz.trinera.dkt.tif2png.TifToPngConvertorImpl;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            File homeDir = new File(System.getProperty("user.home"));

            //init configuration
            File configFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/config.properties");
            //TODO: use CLI to set config file
            Config.init(configFile);
            System.out.println(Config.instanceOf());

            /*File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane");
            File pngInputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png");
            File workingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-processing");
            File ndkPackageWorkingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-ndk-package");
            File resultsDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-results");*/

            //SAMPLE1: 0001.tif - 0027.tif (3 packages), ~/TrineraProjects/KramarskeTisky/data/input/sample1
            File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/input");
            File pngInputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/_png-input");
            File workingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/_working");
            File ndkPackageWorkingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/_ndk-package");
            File resultsDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/sample1/_results");

            System.out.println("Preparing digitization workflow");
            System.out.println("Input dir: " + inputDir.getAbsolutePath());
            System.out.println("Working dir: " + workingDir.getAbsolutePath());
            System.out.println("NDK package working dir: " + ndkPackageWorkingDir.getAbsolutePath());
            System.out.println("Kramerius import dir: " + resultsDir.getAbsolutePath());
            System.out.println();

            DigitizationWorkflow digitizationWorkflow = getDigitizationWorkflow(homeDir);
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

    private static DigitizationWorkflow getDigitizationWorkflow(File homeDir) throws ToolAvailabilityError {
        //TifToPngConvertor tifToPngConvertor = new TifToPngConvertorImpl(Config "src/main/resources/tif2png/check_imagemagick.sh", "src/main/resources/tif2png/convert_tifs_to_pngs.sh");
        TifToPngConvertor tifToPngConvertor = new TifToPngConvertorImpl(
                Config.instanceOf().getTifToPngConvertorLibrariesCheckScript(),
                Config.instanceOf().getTifToPngConvertorScript()
        );

        //BarcodeDetector barcodeDetector = new BarcodeDetectorPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");
        BarcodeDetector barcodeDetector = new BarcodeDetectorImplPyzbar(
                Config.instanceOf().getBarcodeDetectorPythonLibrariesCheckScript(),
                Config.instanceOf().getBarcodeDetectorPythonScript()
        );

        //OcrProvider ocrProvider = new OcrProviderMock();
        OcrProvider ocrProvider = new OcrProviderImpl(
                Config.instanceOf().getOcrProviderPeroBaseUrl(),
                Config.instanceOf().getOcrProviderPeroApiKey(),
                Config.instanceOf().getOcrProviderPeroEngineId()
        );

        Jp2kConvertor jp2kConvertor = new Jp2kConvertorMock(); //TODO: use proper implementation in production
        MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock(); //TODO: use proper implementation in production

        //File marcToModsXsltFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/xslt/MARC21slim2MODS3.xsl");
        //MarcToModsConvertor marcToModsConvertor = new MarcToModsConvertorImpl(marcToModsXsltFile.getAbsolutePath());
        MarcToModsConvertor marcToModsConvertor = new MarcToModsConvertorImpl(
                Config.instanceOf().getMarcxmlToModsConvertorXsltFile()
        );

        //check availability of all components
        tifToPngConvertor.checkAvailable();
        barcodeDetector.checkAvailable();
        ocrProvider.checkAvailable();
        jp2kConvertor.checkAvailable();
        marcXmlProvider.checkAvailable();
        marcToModsConvertor.checkAvailable();

        return new DigitizationWorkflow(tifToPngConvertor, barcodeDetector, ocrProvider, jp2kConvertor, marcXmlProvider, marcToModsConvertor);
    }
}