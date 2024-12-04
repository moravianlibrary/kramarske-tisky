package cz.trinera.dkt.marc21;

import cz.trinera.dkt.AvailabilityError;
import nu.xom.Document;

public interface MarcXmlProvider {

    public Document getMarcXml(String barcode);

    public void checkAvailable() throws AvailabilityError;
}
