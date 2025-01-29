package cz.trinera.dkt.mods2dc;

import cz.trinera.dkt.ToolAvailabilityError;
import cz.trinera.dkt.Utils;
import nu.xom.Document;

import java.io.File;

public class ModsToDcConverterImpl implements ModsToDcConverter {

    private final File xsltFile;

    public ModsToDcConverterImpl(String xsltFilePath) {
        this.xsltFile = new File(xsltFilePath);
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        if (!xsltFile.exists()) {
            throw new ToolAvailabilityError("MarcToDcConverter: XSLT file " + xsltFile.getAbsolutePath() + " does not exist");
        } else if (!xsltFile.canRead()) {
            throw new ToolAvailabilityError("MarcToDcConverter: XSLT file " + xsltFile.getAbsolutePath() + " is not readable");
        } else {
            try {
                //test parse the xslt file
                Utils.loadXmlFromFile(xsltFile);
            } catch (Throwable e) {
                throw new ToolAvailabilityError("MarcToDcConverter: XSLT file " + xsltFile.getAbsolutePath() + " is not a valid XML file", e);
            }
        }
    }

    @Override
    public Document convertModsToDc(Document marcXml) {
        Document dcDoc = Utils.convertDocumentWithXslt(marcXml, xsltFile);
        return dcDoc;
    }
}
