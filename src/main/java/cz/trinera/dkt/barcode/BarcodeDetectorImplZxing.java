package cz.trinera.dkt.barcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import cz.trinera.dkt.ToolAvailabilityError;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BarcodeDetectorImplZxing implements BarcodeDetector {

    public Barcode detect(File pngFile) {
        try {
            // Load the image
            BufferedImage bufferedImage = ImageIO.read(pngFile);

            // Convert image to binary bitmap source
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            // Try to decode the barcode
            Reader reader = new MultiFormatReader(); // Supports multiple formats
            Result result = reader.decode(bitmap);

            // Print the barcode text and format
            System.out.println("Barcode Found!");
            System.out.println("Text: " + result.getText());
            System.out.println("Format: " + result.getBarcodeFormat());

            return new Barcode(result.getBarcodeFormat().toString(), result.getText());
        } catch (NotFoundException e) {
            System.out.println("No barcode found in the image.");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        // TODO: Check if the required libraries are available
    }

}
