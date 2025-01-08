package cz.trinera.dkt;

import cz.trinera.dkt.marc21.Marc21ToMarcXmlConverter;
import nu.xom.Document;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class Marc21ToMarcXmlConvertorTest {

    Marc21ToMarcXmlConverter marc21ToMarcXmlConvertor = new Marc21ToMarcXmlConverter();

    @Test
    public void testConvertSampleFile() {
        try {
            File inputFile = new File("src/main/resources/marc21ToMarcxml/sample1.txt");
            File outputFile = new File("src/main/resources/marc21ToMarcxml/sample1.xml");
            marc21ToMarcXmlConvertor.convert(inputFile, outputFile);
            Document marcXml = Utils.loadXmlFromFile(outputFile);
            boolean valid = Utils.validateXmlAgainstXsd(marcXml, new File("src/main/resources/marc21ToMarcxml/MARC21slim.xsd"));
            assertTrue(valid);
        } catch (Exception e) {
            fail(e);
        }
    }
}
