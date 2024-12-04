package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorPyzbar;
import cz.trinera.dkt.jp2k.Jp2kConvertor;
import cz.trinera.dkt.jp2k.Jp2kConvertorMock;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderMock;
import cz.trinera.dkt.marc2mods.MarcToModsConvertor;
import cz.trinera.dkt.marc2mods.MarcToModsConvertorImpl;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderMock;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            File homeDir = new File(System.getProperty("user.home"));
            File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png");
            File workingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-processing");
            File ndkPackageWorkingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-ndk-package");
            File resultsDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-results");

            System.out.println("Preparing digitization workflow");
            System.out.println("Input dir: " + inputDir.getAbsolutePath());
            System.out.println("Working dir: " + workingDir.getAbsolutePath());
            System.out.println("NDK package working dir: " + ndkPackageWorkingDir.getAbsolutePath());
            System.out.println("Kramerius import dir: " + resultsDir.getAbsolutePath());
            System.out.println();

            DigitizationWorkflow digitizationWorkflow = getDigitizationWorkflow(homeDir);
            System.out.println("Running digitization workflow");
            digitizationWorkflow.run(inputDir, workingDir, ndkPackageWorkingDir, resultsDir);
        } catch (ToolAvailabilityError e) {
            System.err.println("Availability error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static DigitizationWorkflow getDigitizationWorkflow(File homeDir) throws ToolAvailabilityError {
        //BarcodeDetector barcodeDetector = new BarcodeDetectorMock();
        BarcodeDetector barcodeDetector = new BarcodeDetectorPyzbar("src/main/resources/barcode/check_pyzbar.py", "src/main/resources/barcode/detect_barcode.py");
        OcrProvider ocrProvider = new OcrProviderMock(); //TODO: use proper implementation in production
        Jp2kConvertor jp2kConvertor = new Jp2kConvertorMock(); //TODO: use proper implementation in production
        MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock(); //TODO: use proper implementation in production
        File marcToModsXsltFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/xslt/MARC21slim2MODS3.xsl");
        MarcToModsConvertor marcToModsConvertor = new MarcToModsConvertorImpl(marcToModsXsltFile);

        //check availability of all components
        barcodeDetector.checkAvailable();
        ocrProvider.checkAvailable();
        jp2kConvertor.checkAvailable();
        marcXmlProvider.checkAvailable();
        marcToModsConvertor.checkAvailable();

        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector, ocrProvider, jp2kConvertor, marcXmlProvider, marcToModsConvertor);
        return digitizationWorkflow;
    }
}