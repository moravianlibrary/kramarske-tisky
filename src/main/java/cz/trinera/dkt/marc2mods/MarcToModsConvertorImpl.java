package cz.trinera.dkt.marc2mods;

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
}
