package cz.trinera.dkt;

import cz.trinera.dkt.marc2mods.MarcToModsConvertor;
import cz.trinera.dkt.marc2mods.MarcToModsConvertorImpl;
import nu.xom.Document;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarcToModsConvertorTest {

    MarcToModsConvertor convertor = new MarcToModsConvertorImpl("src/main/resources/xslt/MARC21slim2MODS3.xsl");
    //MarcToModsConvertor convertor = new MarcToModsConvertorImpl("src/main/resources/xslt/MARC21slim2MODS3NoIncludes.xsl");

    @Test
    public void convertSample1() {
        //1st package - image 0001.png, barcode 2610798805
        File marcxmlInputFile = new File("src/main/resources/marcxml/marcxml-sample-1.xml");
        Document marcDoc = Utils.loadXmlFromFile(marcxmlInputFile);
        Document converted = convertor.convertMarcToMods(marcDoc);
        //System.out.println(converted.toXML());
        Document expectedResult = Utils.loadXmlFromFile(new File("src/main/resources/mods/mods-sample-1.xml"));
        //assertEquals(expectedResult.toXML(), converted.toXML());
        assertEquals(Utils.prettyPrintDocument(expectedResult), Utils.prettyPrintDocument(converted));
    }

    @Test
    public void convertSample2() {
        //3rd package - image 0019.png, barcode 2610798810
        File marcxmlInputFile = new File("src/main/resources/marcxml/marcxml-sample-2.xml");
        Document marcDoc = Utils.loadXmlFromFile(marcxmlInputFile);
        Document converted = convertor.convertMarcToMods(marcDoc);
        //System.out.println(converted.toXML());
        Document expectedResult = Utils.loadXmlFromFile(new File("src/main/resources/mods/mods-sample-2.xml"));
        //assertEquals(expectedResult.toXML(), converted.toXML());
        assertEquals(Utils.prettyPrintDocument(expectedResult), Utils.prettyPrintDocument(converted));
    }

    @Test
    public void convertSample3() {
        //converted from marc21
        File marcxmlInputFile = new File("src/main/resources/marc21ToMarcxml/sample1.xml");
        Document marcDoc = Utils.loadXmlFromFile(marcxmlInputFile);
        Document converted = convertor.convertMarcToMods(marcDoc);
        //System.out.println(converted.toXML());
        Document expectedResult = Utils.loadXmlFromFile(new File("src/main/resources/marc21ToMarcxml/sample1_mods.xml"));
        //assertEquals(expectedResult.toXML(), converted.toXML());
        assertEquals(Utils.prettyPrintDocument(expectedResult), Utils.prettyPrintDocument(converted));
    }
}
