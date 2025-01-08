package cz.trinera.dkt;

import cz.trinera.dkt.marc21.MarcXmlProvider;
import cz.trinera.dkt.marc21.MarcXmlProviderImplYazClient;
import nu.xom.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MarcXmlProviderTest {

    @BeforeAll
    public static void setUp() throws IOException {
        File homeDir = new File(System.getProperty("user.home"));
        File configFile = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/config.properties");
        Config.init(configFile);
    }

    //MarcXmlProvider marcXmlProvider = new MarcXmlProviderMock();
    MarcXmlProvider marcXmlProvider = new MarcXmlProviderImplYazClient(
            "src/main/resources/marc21/check_yaz_client.py",
            "src/main/resources/marc21/fetch_marc21_by_barcode.py",
            "aleph.mzk.cz", 9991, "MZK03CPK");

    @Test
    public void convertSample05() {
        convertSample("2610798805");
    }

    @Test
    public void convertSample806() {
        convertSample("2610798806");
    }

    @Test
    public void convertSample810() {
        convertSample("2610798810");
    }

    @Test
    public void convertSample809() {
        convertSample("2610798809");
    }

    @Test
    public void convertSample808() {
        convertSample("2610798808");
    }

    @Test
    public void convertSample807() {
        convertSample("2610798807");
    }

    @Test
    public void convertSample803() {
        convertSample("2610798803");
    }

    @Test
    public void convertSample804() {
        convertSample("2610798804");
    }

    @Test
    public void convertSample798() {
        convertSample("2610798798");
    }

    @Test
    public void convertSample797() {
        convertSample("2610798797");
    }

    private void convertSample(String barcode) {
        Document marcDoc = marcXmlProvider.getMarcXml(barcode);
        assertNotNull(marcDoc);
        assertEquals("record", marcDoc.getRootElement().getLocalName());
        assertEquals("http://www.loc.gov/MARC21/slim", marcDoc.getRootElement().getNamespaceURI());
        //System.out.println(marcDoc.toXML());
        boolean valid = Utils.validateXmlAgainstXsd(marcDoc, new File("src/main/resources/marc21ToMarcxml/MARC21slim.xsd"));
        assertTrue(valid);
    }

    @Test
    public void checkMarcXmlProvider() {
        try {
            marcXmlProvider.checkAvailable();
        } catch (ToolAvailabilityError e) {
            fail(e.getMessage(), e);
        }
    }
}
