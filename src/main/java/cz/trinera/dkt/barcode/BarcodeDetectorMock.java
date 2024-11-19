package cz.trinera.dkt.barcode;

import java.io.File;

public class BarcodeDetectorMock implements BarcodeDetector {

    @Override
    public Barcode detect(File file) {

        String[] barcodeFiles = new String[]{
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
        };
        if (file.getName().equals(barcodeFiles[0])) {
            return new Barcode("mock", "123");
        } else {
            return null;
        }
    }
}
