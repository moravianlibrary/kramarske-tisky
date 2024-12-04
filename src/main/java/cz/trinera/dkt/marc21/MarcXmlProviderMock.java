package cz.trinera.dkt.marc21;

import cz.trinera.dkt.AvailabilityError;
import cz.trinera.dkt.Utils;
import nu.xom.Document;

import java.io.File;

public class MarcXmlProviderMock implements MarcXmlProvider {
    @Override
    public Document getMarcXml(String barcode) {
        File homeDir = new File(System.getProperty("user.home"));
        File marcXmlSample = new File(homeDir.getAbsolutePath() + "/TrineraProjects/KramarskeTisky/dkt-workflow/src/main/resources/marcxml/marcxml-sample.xml");
        return Utils.loadXmlFromFile(marcXmlSample);
    }

    @Override
    public void checkAvailable() throws AvailabilityError {
        //do nothing in mock implementation
    }
}
