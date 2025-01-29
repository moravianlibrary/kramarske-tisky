package cz.trinera.dkt.mods2dc;

import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;

public interface ModsToDcConverter {

    public void checkAvailable() throws ToolAvailabilityError;

    public Document convertModsToDc(Document marcXml);

}
