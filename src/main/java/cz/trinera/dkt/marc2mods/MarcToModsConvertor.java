package cz.trinera.dkt.marc2mods;

import cz.trinera.dkt.AvailabilityError;
import nu.xom.Document;

public interface MarcToModsConvertor {

    public Document convertMarcToMods(Document marcXml);

    public void checkAvailable() throws AvailabilityError;
}
