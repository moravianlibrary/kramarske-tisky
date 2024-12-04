package cz.trinera.dkt.marc21;

import nu.xom.Document;

public interface MarcXmlProvider {

    public Document getMarcXml(String barcode);

    //TODO
    //public void checkAvailable() throws AvailabilityError;
}
