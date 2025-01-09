package cz.trinera.dkt.marc2mods;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;
import nu.xom.Document;

import java.io.File;

public class MarcToModsConverterImpl implements MarcToModsConverter {

    private final File xsltFile;

    public MarcToModsConverterImpl(String xsltFilePath) {
        this.xsltFile = new File(xsltFilePath);
    }

    @Override
    public Document convertMarcToMods(Document marcXml) {
        Document modsDoc = Utils.convertDocumentWithXslt(marcXml, xsltFile);
        //TODO: enrich MODS some more (or within the XSLT)
        return modsDoc;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        if (!xsltFile.exists()) {
            throw new ToolAvailabilityError("MarcToModsConverter: XSLT file " + xsltFile.getAbsolutePath() + " does not exist");
        } else if (!xsltFile.canRead()) {
            throw new ToolAvailabilityError("MarcToModsConverter: XSLT file " + xsltFile.getAbsolutePath() + " is not readable");
        } else {
            try {
                //test parse the xslt file
                Utils.loadXmlFromFile(xsltFile);
            } catch (Throwable e) {
                throw new ToolAvailabilityError("MarcToModsConverter: XSLT file " + xsltFile.getAbsolutePath() + " is not a valid XML file", e);
            }
        }
    }
}
