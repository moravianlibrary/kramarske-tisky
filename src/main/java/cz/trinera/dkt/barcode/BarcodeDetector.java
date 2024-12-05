package cz.trinera.dkt.barcode;

import cz.trinera.dkt.ToolAvailabilityError;

import java.io.File;

public interface BarcodeDetector {

    /**
     * Searches for a barcode in the given PNG file. Preferably barcode Code39.
     *
     * @param pngFile image file possibly containing a barcode
     * @return Barcode if detected, null otherwise
     */
    public Barcode detect(File pngFile);

    public void checkAvailable() throws ToolAvailabilityError;

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

        @Override
        public String toString() {
            return "Barcode{" +
                    "format='" + format + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
