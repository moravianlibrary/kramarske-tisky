package cz.trinera.dkt.marc2mods;

import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;

public interface MarcToModsConverter {

    public void checkAvailable() throws ToolAvailabilityError;

    public Document convertMarcToMods(Document marcXml);
}
