package cz.trinera.dkt;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class BarcodeDetector {

    public Barcode detect(File file) {
        try {
            // Load the image
            BufferedImage bufferedImage = ImageIO.read(file);

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

    public static class Barcode {
        private final String format;
        private final String value;

        public Barcode(String format, String value) {
            this.format = format;
            this.value = value;
        }

        public String getFormat() {
            return format;
        }

        public String getValue() {
            return value;
        }
    }

}
