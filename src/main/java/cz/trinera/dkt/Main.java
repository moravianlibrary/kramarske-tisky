package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;
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

            BarcodeDetector barcodeDetector = new BarcodeDetectorMock(); //TODO: use proper implementation in production
            OcrProvider ocrProvider = new OcrProviderMock(); //TODO: use proper implementation in production
            Jp2kConvertor jp2kConvertor = new Jp2kConvertorMock(); //TODO: use proper implementation in production
            MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock(); //TODO: use proper implementation in production
            File marcToModsXsltFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/xslt/MARC21slim2MODS3.xsl");
            MarcToModsConvertor marcToModsConvertor = new MarcToModsConvertorImpl(marcToModsXsltFile);

            //check availability of all components
            barcodeDetector.checkAvailable();
            //TODO: check remaining components

            DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector, ocrProvider, jp2kConvertor, marcXmlProvider, marcToModsConvertor);
            digitizationWorkflow.run(inputDir, workingDir, ndkPackageWorkingDir, resultsDir);
        } catch (AvailabilityError e) {
            System.err.println("Availability error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}