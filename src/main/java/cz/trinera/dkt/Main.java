package cz.trinera.dkt;

import cz.trinera.dkt.barcode.BarcodeDetector;
import cz.trinera.dkt.barcode.BarcodeDetectorMock;
import cz.trinera.dkt.jp2k.Jp2kConvertor;
import cz.trinera.dkt.jp2k.Jp2kConvertorMock;
import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderMock;
import cz.trinera.dkt.ocr.OcrProvider;
import cz.trinera.dkt.ocr.OcrProviderMock;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        File homeDir = new File(System.getProperty("user.home"));
        File inputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png");
        File workingDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-processing");
        File outputDir = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/data/input/orezane-png-results");

        BarcodeDetector barcodeDetector = new BarcodeDetectorMock(); //TODO: use proper implementation in production
        OcrProvider ocrProvider = new OcrProviderMock(); //TODO: use proper implementation in production
        Jp2kConvertor jp2kConvertor = new Jp2kConvertorMock(); //TODO: use proper implementation in production
        MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock(); //TODO: use proper implementation in production

        DigitizationWorkflow digitizationWorkflow = new DigitizationWorkflow(barcodeDetector, ocrProvider, jp2kConvertor, marcXmlProvider);
        digitizationWorkflow.run(inputDir, workingDir, outputDir);
    }
}