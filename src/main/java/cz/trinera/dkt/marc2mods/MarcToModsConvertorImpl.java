package cz.trinera.dkt.marc2mods;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;
import nu.xom.Document;

import java.io.File;

public class MarcToModsConvertorImpl implements MarcToModsConvertor {

    private final File xsltFile;

    public MarcToModsConvertorImpl(File xsltFile) {
        this.xsltFile = xsltFile;
        //TODO: load and test the file
    }

    @Override
    public Document convertMarcToMods(Document marcXml) {
        Document modsDoc = Utils.convertDocumentWithXslt(marcXml, xsltFile);
        //TODO: enrich MODS some more
        return modsDoc;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        if (!xsltFile.exists()) {
            throw new ToolAvailabilityError("MarcToModsConvertor: XSLT file " + xsltFile.getAbsolutePath() + " does not exist");
        } else if (!xsltFile.canRead()) {
            throw new ToolAvailabilityError("MarcToModsConvertor: XSLT file " + xsltFile.getAbsolutePath() + " is not readable");
        } else {
            try {
                //test parse the xslt file
                Utils.loadXmlFromFile(xsltFile);
            } catch (Throwable e) {
                throw new ToolAvailabilityError("MarcToModsConvertor: XSLT file " + xsltFile.getAbsolutePath() + " is not a valid XML file", e);
            }
        }
    }
}
