package cz.trinera.dkt.barcode;

import cz.trinera.dkt.AvailabilityError;

import java.io.File;

public interface BarcodeDetector {

    public Barcode detect(File file);

    public void checkAvailable() throws AvailabilityError;

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
