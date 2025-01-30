package cz.trinera.dkt;

import cz.trinera.dkt.utils.MonographMetadataExtractor;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MonographMetadataExtractorTest {

    @Test
    public void extractTitle1() {
        try {
            File modsFile = new File("src/main/resources/mods/mods-sample-1.xml");
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(modsFile, null);
            String title = extractor.extractTitle();
            assertEquals("Pjsně dwě k Pánu GEžjssy", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void extractTitle2() {
        try {
            File modsFile = new File("src/main/resources/mods/mods-sample-2.xml");
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(modsFile, null);
            String title = extractor.extractTitle();
            assertEquals("Pjseň Postnj ku Pánu Gežjssy", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void extractTitle3() {
        try {
            File modsFile = new File("src/main/resources/mods/mods-sample-3.xml");
            File dcFile = new File("src/main/resources/mods/dc-sample-3.xml");
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(modsFile, dcFile);
            String title = extractor.extractTitle();
            assertEquals("Pjsně dwě k Pánu GEžjssy", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void extractTitle3Mods() {
        try {
            File modsFile = new File("src/main/resources/mods/mods-sample-3.xml");
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(modsFile, null);
            String title = extractor.extractTitle();
            assertEquals("Pjsně dwě k Pánu GEžjssy", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void extractTitle3Dc() {
        try {
            File dcFile = new File("src/main/resources/mods/dc-sample-3.xml");
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(null, dcFile);
            String title = extractor.extractTitle();
            assertEquals("Pjsně dwě k Pánu GEžjssy", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Test
    public void extractTitleEmpty() {
        try {
            MonographMetadataExtractor extractor = new MonographMetadataExtractor(null, null);
            String title = extractor.extractTitle();
            assertEquals("", title);
        } catch (Throwable e) {
            fail(e);
        }
    }

}
