package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;

public class AmdSecBuilder {

    private static String NS_DOCMD = "http://www.fcla.edu/docmd";
    private static String NS_METS = "http://www.loc.gov/METS/";
    private static String NS_NDKTECH = "http://www.ndk.cz/standardy-digitalizace/ndktech";
    private static String NS_NK = "info:ndk/xmlns/nk-v1";
    private static String NS_PREMIS = "info:lc/xmlns/premis-v2";
    private static String NS_XLINK = "http://www.w3.org/1999/xlink";

    private File ndkPackageDir;
    private UUID packageUuid;
    private Timestamp now;

    public AmdSecBuilder(File ndkPackageDir, UUID packageUuid, Timestamp now) {
        this.ndkPackageDir = ndkPackageDir;
        this.packageUuid = packageUuid;
        this.now = now;
    }

    public void buildAndSavePage(int pageNumber) {
        Document doc = buildAmdSec(pageNumber);
        Utils.saveDocumentToFile(doc, new File(ndkPackageDir, "amdSec_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".xml"));
    }

    private Document buildAmdSec(int pageNumber) {
        Element rootEl = new Element("mets", "http://www.loc.gov/METS/");
        rootEl.addAttribute(new Attribute("LABEL", "TODO: nazev monografie"));
        rootEl.addAttribute(new Attribute("TYPE", "Monograph"));
        appendMetsHdr(rootEl);
        appendAmdSec(rootEl, pageNumber);
        appendFileSec(rootEl);
        appendStructMap(rootEl);
        return new Document(rootEl);
    }

    private void appendStructMap(Element parentEl) {
        Element structMapEl = addNewMetsEl(parentEl, "structMap");
        //TODO: fill structMap
    }

    private void appendFileSec(Element parentEl) {
        Element fileSecEl = addNewMetsEl(parentEl, "fileSec");
        //TODO: fill fileSec
    }

    private void appendAmdSec(Element parentEl, int pageNumber) {
        Element amdSecEl = addNewMetsEl(parentEl, "amdSec");
        //TODO: fill amdSec
    }

    private void appendMetsHdr(Element rootEl) {
        Element metsHdrEl = addNewMetsEl(rootEl, "metsHdr");
        String nowFormatted = now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
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
