package cz.trinera.dkt.utils;

import cz.trinera.dkt.Utils;
import nu.xom.Document;
import nu.xom.Nodes;

import java.io.File;

public class MonographMetadataExtractor {

    private final File modsFile;
    private final File dcFile;
    private Document modsDoc;
    private Document dcDoc;

    public MonographMetadataExtractor(File modsFile, File dcFile) {
        this.modsFile = modsFile;
        this.dcFile = dcFile;
    }

    /**
     * Extracts the title from the MODS or DC metadata.
     *
     * @return the title or an empty string if no title found (never null)
     */
    public String extractTitle() {
        String fromMods = extractTitleFromMods();
        if (fromMods != null) {
            return fromMods;
        }
        String fromDc = extractTitleFromDc();
        if (fromDc != null) {
            return fromDc;
        }
        return "";
    }

    private String extractTitleFromDc() {
        Document dcDoc = getDcDoc();
        if (dcDoc == null) {
            return null;
        }
        Nodes titles = dcDoc.query("//*[local-name()='title']");
        if (titles.size() > 0) {
            return titles.get(0).getValue();
        } else {
            System.out.println("No title found");
        }
        return null;
    }

    private String extractTitleFromMods() {
        Document modsDoc = getModsDoc();
        if (modsDoc == null) {
            return null;
        }
        // only those titleInfos that don't have type="alternative"
        Nodes titlesPrimary = modsDoc.query("//*[local-name()='mods']/*[local-name()='titleInfo'][not(@type='alternative')]/*[local-name()='title']");
        if (titlesPrimary.size() > 0) {
            return titlesPrimary.get(0).getValue();
        } else {
            System.out.println("No primary title found");
        }
        // if no primary title found, use the first titleInfo (can be type="alternative")
        Nodes titlesAll = modsDoc.query("//*[local-name()='mods']/*[local-name()='titleInfo']/*[local-name()='title']");
        if (titlesAll.size() > 0) {
            return titlesAll.get(0).getValue();
        } else {
            System.out.println("No title found");
        }
        return null;
    }

    private Document getModsDoc() {
        if (modsDoc == null && modsFile != null) {
            modsDoc = Utils.loadXmlFromFile(modsFile);
        }
        return modsDoc;
    }

    private Document getDcDoc() {
        if (dcDoc == null && dcFile != null) {
            dcDoc = Utils.loadXmlFromFile(dcFile);
        }
        return dcDoc;
    }

}





