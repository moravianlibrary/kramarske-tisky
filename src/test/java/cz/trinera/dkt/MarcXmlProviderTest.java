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
    public void convertSample1() {
        String barcode = "2610798805";
        Document marcDoc = marcXmlProvider.getMarcXml(barcode);
        assertTrue(marcDoc != null);
        //TODO: check value
        System.out.println(marcDoc.toXML());
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
