package cz.trinera.dkt.barcode;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BarcodeDetectorMock implements BarcodeDetector {

    private final Random random = new Random();

    @Override
    public Barcode detect(File file) {

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
        if (barcodeFiles.contains(file.getName())) {
            return new Barcode("mock", file.getName() + "_" + randomString(5));
        } else {
            return null;
        }
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
}
