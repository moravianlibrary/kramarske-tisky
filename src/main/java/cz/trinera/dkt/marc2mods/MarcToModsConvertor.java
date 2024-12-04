package cz.trinera.dkt.marc2mods;

import nu.xom.Document;

public interface MarcToModsConvertor {

    public Document convertMarcToMods(Document marcXml);

    //TODO
    //public void checkAvailable() throws AvailabilityError;
}
