package cz.trinera.dkt.barcode;

import java.io.File;

public interface BarcodeDetector {

    public Barcode detect(File file);

    public class Barcode {
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
