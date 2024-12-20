package cz.trinera.dkt;

import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderImpl;
import cz.trinera.dkt.marc21.MarcXmlProviderMock;
import nu.xom.Document;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class MarcXmlProviderTest {

    MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock();
    //TODO: use real implementation
    //MarcXmlProvider marcXmlProvider = new MarcXmlProviderImpl("aleph.mzk.cz", 9991, "MZK03CPK");

    @Test
    public void convertSample1() {
        String barcode = "2610798805";
        Document marcDoc = marcXmlProvider.getMarcXml(barcode);
        assertTrue(marcDoc != null);
        //TODO: check value
        System.out.println(marcDoc.toXML());
    }

}
