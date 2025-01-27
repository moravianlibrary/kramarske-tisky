package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

public class MainMetsBuilder {
    private static String NS_DOCMD = "http://www.fcla.edu/docmd";
    private static String NS_METS = "http://www.loc.gov/METS/";
    private static String NS_NDKTECH = "http://www.ndk.cz/standardy-digitalizace/ndktech";
    private static String NS_NK = "info:ndk/xmlns/nk-v1";
    private static String NS_PREMIS = "info:lc/xmlns/premis-v2";
    private static String NS_XLINK = "http://www.w3.org/1999/xlink";

    private final File ndkPackageDir;
    private final UUID packageUuid;
    private final Timestamp now;
    private final String nowFormatted;

    public MainMetsBuilder(File ndkPackageDir, UUID packageUuid, Timestamp now) {
        this.ndkPackageDir = ndkPackageDir;
        this.packageUuid = packageUuid;
        this.now = now;
        this.nowFormatted = now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public Document build(Set<FileInfo> fileInfos, String monographTitle) {
        Element rootEl = new Element("mets", "http://www.loc.gov/METS/");
        rootEl.addAttribute(new Attribute("LABEL", monographTitle));
        rootEl.addAttribute(new Attribute("TYPE", "Monograph"));
        appendMetsHdr(rootEl);
        //TODO: append dmdSec (MODS, DC for each page, volume, monograph)
        //TODO: append structMap (logical, physical)
        //TODO: append fileSec
        //TODO: append structLink
        return new Document(rootEl);
    }

    private void appendMetsHdr(Element rootEl) {
        Element metsHdrEl = addNewMetsEl(rootEl, "metsHdr");
        metsHdrEl.addAttribute(new Attribute("CREATEDATE", nowFormatted));
        metsHdrEl.addAttribute(new Attribute("LASTMODDATE", nowFormatted));
        //agent CREATOR
        Element agentCreatorEl = addNewMetsEl(metsHdrEl, "agent");
        agentCreatorEl.addAttribute(new Attribute("ROLE", "CREATOR"));
        agentCreatorEl.addAttribute(new Attribute("TYPE", "ORGANIZATION"));
        Element agentCreatorNameEl = addNewMetsEl(agentCreatorEl, "name");
        agentCreatorNameEl.appendChild("CreatorMZK");
        //agent ARCHIVIST
        Element agentArchivistEl = addNewMetsEl(metsHdrEl, "agent");
        agentArchivistEl.addAttribute(new Attribute("ROLE", "ARCHIVIST"));
        agentArchivistEl.addAttribute(new Attribute("TYPE", "ORGANIZATION"));
        Element agentArchivistNameEl = addNewMetsEl(agentArchivistEl, "name");
        agentArchivistNameEl.appendChild("ArchivistMZK");
    }

    private Element addNewMetsEl(Element parentEl, String elName) {
        Element element = new Element(elName, NS_METS);
        parentEl.appendChild(element);
        return element;
    }
}
