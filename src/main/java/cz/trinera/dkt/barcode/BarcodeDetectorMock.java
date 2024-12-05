package cz.trinera.dkt.barcode;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;
import java.util.Random;
import java.util.Set;

public class BarcodeDetectorMock implements BarcodeDetector {

    private final Random random = new Random();

    @Override
    public Barcode detect(File pngFile) {

        Set<String> barcodeFiles = Set.of(
                "0001.png",
                "0010.png",
                "0019.png",
                "0028.png",
                "0037.png",
                "0046.png",
                "0055.png",
                "0064.png",
                "0073.png",
                "0082.png"
        );
        if (barcodeFiles.contains(pngFile.getName())) {
            //return new Barcode("mock", file.getName() + "_" + randomString(5));
            return new Barcode("mock", randomNumbers(10));
        } else {
            return null;
        }
    }

    private String randomNumbers(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + "0123456789";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        // Do nothing
        //throw new AvailabilityError("Barcode mock detector is not available.");
    }
}


