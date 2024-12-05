package cz.trinera.dkt.barcode;

import boofcv.alg.feature.detect.edge.GGradientToEdgeFeatures;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayU8;
import cz.trinera.dkt.ToolAvailabilityError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BarcodeDetectorImplBoofCv implements BarcodeDetector {

    public Barcode detect(File pngFile) {
        // Load the image file
        /*BufferedImage image = UtilImageIO.loadImage(file.getAbsolutePath());

        if (image == null) {
            System.out.println("Could not load the image.");
            return null;
        }*/

        // Load the image
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(pngFile);
        } catch (IOException e) {
            System.err.println("Failed to load image.");
            e.printStackTrace();
            return null;
        }

        // Convert BufferedImage to BoofCV's GrayU8 format
        GrayU8 image = ConvertBufferedImage.convertFrom(bufferedImage, (GrayU8) null);

        // Apply edge detection to find features of the barcode
        GrayU8 edgeImage = new GrayU8(image.width, image.height);
        GGradientToEdgeFeatures.intensityAbs(image, edgeImage, null);

        // Threshold to create a binary image
        GrayU8 binaryImage = new GrayU8(image.width, image.height);
        FactoryThresholdBinary.globalOtsu(0, 255, 1.0, true, GrayU8.class).process(edgeImage, binaryImage);

        // Process the binary image to detect contours
        List<Contour> contours = BinaryImageOps.contour(binaryImage, ConnectRule.EIGHT, null);

        // Analyze contours for barcode-like patterns
        if (contours.isEmpty()) {
            System.out.println("No potential barcodes found.");
        } else {
            for (Contour contour : contours) {
                System.out.println("Contour found with " + contour.external.size() + " points.");
                // Further process the contour to verify if it's a barcode
                // In this version, you may need to integrate an external decoder (e.g., ZXing).
            }
        }


       /* // Convert image to grayscale
        GrayU8 gray = ConvertBufferedImage.convertFrom(image, (GrayU8) null);

        // Create a 1D barcode detector
        etectBarcode1D<GrayU8> detector = FactoryFiducial.barcode1D(null, GrayU8.class);

        // Detect barcodes in the image
        detector.process(image);

        // Get the list of detected barcodes
        List<BarcodePositionPatternNode> barcodes = detector.getFound();

        if (barcodes.isEmpty()) {
            System.out.println("No barcodes found.");
        } else {
            for (BarcodePositionPatternNode barcode : barcodes) {
                System.out.println("Detected Barcode: " + barcode.message);
                System.out.println("Position: " + barcode.bounds.toString());
            }
        }*/

       /* // Create a QR Code detector (BoofCV can detect multiple types of barcodes)
        QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(null, GrayU8.class);

        // Detect barcodes in the image
        detector.process(gray);

        // Print detected QR codes
        detector.getDetections().forEach(qr -> {
            System.out.println("Detected QR Code:");
            System.out.println("Message: " + qr.message);
            System.out.println("Position: " + qr.bounds.toString());
        });

        // Print detection failures
        detector.getFailures().forEach(failure -> {
            System.out.println("Failed Detection:");
            System.out.println("Position: " + failure.bounds.toString());
        });*/

        return null;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        // TODO: Check if the required libraries are available
    }
}
