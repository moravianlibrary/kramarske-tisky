package cz.trinera.dkt.ndk;

import cz.trinera.dkt.NamedPage;
import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainMetsBuilder {
    private static String NS_DOCMD = "http://www.fcla.edu/docmd";
    private static String NS_METS = "http://www.loc.gov/METS/";
    private static String NS_NDKTECH = "http://www.ndk.cz/standardy-digitalizace/ndktech";
    private static String NS_NK = "info:ndk/xmlns/nk-v1";
    private static String NS_PREMIS = "info:lc/xmlns/premis-v2";
    private static String NS_XLINK = "http://www.w3.org/1999/xlink";
    private static String NS_OAIDC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    private static String NS_DC = "http://purl.org/dc/elements/1.1/";
    private static String NS_MODS = "http://www.loc.gov/mods/v3";

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

    public Document build(Set<FileInfo> fileInfos, String monographTitle, List<NamedPage> pages) {
        //Element rootEl = new Element("mets", "http://www.loc.gov/METS/");
        Element rootEl = addNewMetsEl(null, "mets");
        rootEl.addAttribute(new Attribute("LABEL", monographTitle));
        rootEl.addAttribute(new Attribute("TYPE", "Monograph"));
        //define namespaces at root
        rootEl.addNamespaceDeclaration("xlink", NS_XLINK);
        rootEl.addNamespaceDeclaration("oai_dc", NS_OAIDC);
        rootEl.addNamespaceDeclaration("dc", NS_DC);
        rootEl.addNamespaceDeclaration("mods", NS_MODS);

        //metsHdr
        appendMetsHdr(rootEl);

        //TODO: append dmdSec (volume: MODS, DC)

        //dmdSec for pages (MODS, DC)
        for (NamedPage page : pages) {
            appendPageDmdSecDC(rootEl, page);
            appendPageDmdSecMODS(rootEl, page);
        }

        //TODO: append structMap (logical, physical)
        //TODO: append fileSec
        //TODO: append structLink
        return new Document(rootEl);
    }

    private void appendPageDmdSecMODS(Element rootEl, NamedPage page) {
        Element dmdSecEl = addNewMetsEl(rootEl, "dmdSec");
        dmdSecEl.addAttribute(new Attribute("ID", "DCMD_PAGE_" + Utils.to4CharNumber(page.getPosition())));
        Element mdWrapEl = addNewMetsEl(dmdSecEl, "mdWrap");
        mdWrapEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
        mdWrapEl.addAttribute(new Attribute("MDTYPE", "DC"));
        Element xmlDataEl = addNewMetsEl(mdWrapEl, "xmlData");
        Element dcEl = addNewElement(xmlDataEl, "oai_dc:dc", NS_OAIDC);
        addNewDcEl(dcEl, "coverage").appendChild("[" + page.getName() + "]");
        addNewDcEl(dcEl, "identifier").appendChild("uuid:" + page.getUuid());
        addNewDcEl(dcEl, "type").appendChild("model:page");
    }

    private void appendPageDmdSecDC(Element rootEl, NamedPage page) {
        //TODO: implement
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
        return addNewElement(parentEl, "mets:" + elName, NS_METS);
    }

    private Element addNewDcEl(Element parentEl, String elName) {
        return addNewElement(parentEl, "dc:" + elName, NS_DC);
    }

    private Element addNewElement(Element parentEl, String elName, String namespace) {
        Element element = new Element(elName, namespace);
        if (parentEl != null) {
            parentEl.appendChild(element);
        }
        return element;
    }

}
