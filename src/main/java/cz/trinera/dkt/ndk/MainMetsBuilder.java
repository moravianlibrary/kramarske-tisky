package cz.trinera.dkt.ndk;

import cz.trinera.dkt.NamedPage;
import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.Comparator;
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
    private static String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static String XSI_SCHEMA_LOCATION = "http://www.w3.org/2001/XMLSchema-instance http://www.w3.org/2001/XMLSchema.xsd http://www.loc.gov/METS/ http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://www.w3.org/1999/xlink http://www.w3.org/1999/xlink.xsd http://www.cdlib.org/groups/rmg/docs/copyrightMD.xsd";
    private static String PACKAGE_CREATOR_CODE = "BOA001"; //Proarc používá "CreatorMZK", ale DMF vyžaduje Sigly
    private static String PACKAGE_ARCHIVIST_CODE = "BOA001"; //Proarc používá "ArchivistMZK", ale DMF vyžaduje Sigly

    private final File ndkPackageDir;
    private final UUID packageUuid;
    private final Timestamp now;
    private final String nowFormatted;
    private final String nowFormattedIso8601;

    public MainMetsBuilder(File ndkPackageDir, UUID packageUuid, Timestamp now) {
        this.ndkPackageDir = ndkPackageDir;
        this.packageUuid = packageUuid;
        this.now = now;
        this.nowFormatted = now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        this.nowFormattedIso8601 = now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }

    public Document build(Set<FileInfo> fileInfos, String monographTitle, List<NamedPage> pages, File modsFile, File dcFile) {
        //Element rootEl = new Element("mets", "http://www.loc.gov/METS/");
        Element rootEl = addNewMetsEl(null, "mets");
        rootEl.addNamespaceDeclaration("xsi", NS_XSI);
        rootEl.addAttribute(new Attribute("xsi:schemaLocation", NS_XSI, XSI_SCHEMA_LOCATION));
        rootEl.addAttribute(new Attribute("LABEL", monographTitle));
        rootEl.addAttribute(new Attribute("TYPE", "Monograph"));
        //define namespaces at root
        rootEl.addNamespaceDeclaration("xlink", NS_XLINK);
        rootEl.addNamespaceDeclaration("oai_dc", NS_OAIDC);
        rootEl.addNamespaceDeclaration("dc", NS_DC);
        rootEl.addNamespaceDeclaration("mods", NS_MODS);

        //metsHdr
        appendMetsHdr(rootEl);

        //dmdSec for volume (MODS, DC)
        appendVolumeDmdSecMods(rootEl, modsFile);
        appendVolumeDmdSecDc(rootEl, dcFile);

        //dmdSec for pages (MODS, DC)
        for (NamedPage page : pages) {
            appendPageDmdSecDC(rootEl, page);
            appendPageDmdSecMODS(rootEl, page);
        }

        //structMap
        appendStructMapLogical(rootEl, monographTitle);
        appendStructMapPhysical(rootEl, monographTitle, pages);

        //fileSec
        appendFileSec(rootEl, fileInfos, pages);

        //structLink
        appendStructLink(rootEl, pages);

        return new Document(rootEl);
    }

    private void appendFileSec(Element rootEl, Set<FileInfo> fileInfos, List<NamedPage> pages) {
        Element fileSecEl = addNewMetsEl(rootEl, "fileSec");

        //MC_IMGGRP
        Element fileGrpMc = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpMc.addAttribute(new Attribute("ID", "MC_IMGGRP"));
        fileGrpMc.addAttribute(new Attribute("USE", "Images"));
        fileInfos.stream()
                .filter(fileInfo -> fileInfo.getCategory() == FileInfo.Category.MC)
                .filter(fileInfo -> fileInfo.getPageNumber() != null)
                .sorted(Comparator.comparing(FileInfo::getPageNumber))
                .forEach(fileInfo -> {
                    Element fileEl = addNewMetsEl(fileGrpMc, "file");
                    fileEl.addAttribute(new Attribute("ID", "mc_" + packageUuid + "_" + Utils.to4CharNumber(fileInfo.getPageNumber())));
                    fileEl.addAttribute(new Attribute("SEQ", (fileInfo.getPageNumber() - 1) + ""));
                    fileEl.addAttribute(new Attribute("MIMETYPE", "image/jp2"));
                    fileEl.addAttribute(new Attribute("SIZE", fileInfo.getFileSizeBytes() + ""));
                    fileEl.addAttribute(new Attribute("CREATED", nowFormatted));
                    fileEl.addAttribute(new Attribute("CHECKSUM", fileInfo.getMd5Checksum()));
                    fileEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
                    Element fLocatEl = addNewMetsEl(fileEl, "FLocat");
                    fLocatEl.addAttribute(new Attribute("xlink:href", NS_XLINK, fileInfo.getPathFromNdkPackageRoot(false)));
                    fLocatEl.addAttribute(new Attribute("LOCTYPE", "URL"));
                });

        //UC_IMGGRP
        Element fileGrpUc = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpUc.addAttribute(new Attribute("ID", "UC_IMGGRP"));
        fileGrpUc.addAttribute(new Attribute("USE", "Images"));
        fileInfos.stream()
                .filter(fileInfo -> fileInfo.getCategory() == FileInfo.Category.UC)
                .filter(fileInfo -> fileInfo.getPageNumber() != null)
                .sorted(Comparator.comparing(FileInfo::getPageNumber))
                .forEach(fileInfo -> {
                    Element fileEl = addNewMetsEl(fileGrpUc, "file");
                    fileEl.addAttribute(new Attribute("ID", "uc_" + packageUuid + "_" + Utils.to4CharNumber(fileInfo.getPageNumber())));
                    fileEl.addAttribute(new Attribute("SEQ", (fileInfo.getPageNumber() - 1) + ""));
                    fileEl.addAttribute(new Attribute("MIMETYPE", "image/jp2"));
                    fileEl.addAttribute(new Attribute("SIZE", fileInfo.getFileSizeBytes() + ""));
                    fileEl.addAttribute(new Attribute("CREATED", nowFormatted));
                    fileEl.addAttribute(new Attribute("CHECKSUM", fileInfo.getMd5Checksum()));
                    fileEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
                    Element fLocatEl = addNewMetsEl(fileEl, "FLocat");
                    fLocatEl.addAttribute(new Attribute("xlink:href", NS_XLINK, fileInfo.getPathFromNdkPackageRoot(false)));
                    fLocatEl.addAttribute(new Attribute("LOCTYPE", "URL"));
                });

        //ALTOGRP
        Element fileGrpAlto = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpAlto.addAttribute(new Attribute("ID", "ALTOGRP"));
        fileGrpAlto.addAttribute(new Attribute("USE", "Layout"));
        fileInfos.stream()
                .filter(fileInfo -> fileInfo.getCategory() == FileInfo.Category.ALTO)
                .filter(fileInfo -> fileInfo.getPageNumber() != null)
                .sorted(Comparator.comparing(FileInfo::getPageNumber))
                .forEach(fileInfo -> {
                    Element fileEl = addNewMetsEl(fileGrpAlto, "file");
                    fileEl.addAttribute(new Attribute("ID", "alto_" + packageUuid + "_" + Utils.to4CharNumber(fileInfo.getPageNumber())));
                    fileEl.addAttribute(new Attribute("SEQ", (fileInfo.getPageNumber() - 1) + ""));
                    fileEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
                    fileEl.addAttribute(new Attribute("SIZE", fileInfo.getFileSizeBytes() + ""));
                    fileEl.addAttribute(new Attribute("CREATED", nowFormatted));
                    fileEl.addAttribute(new Attribute("CHECKSUM", fileInfo.getMd5Checksum()));
                    fileEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
                    Element fLocatEl = addNewMetsEl(fileEl, "FLocat");
                    fLocatEl.addAttribute(new Attribute("xlink:href", NS_XLINK, fileInfo.getPathFromNdkPackageRoot(false)));
                    fLocatEl.addAttribute(new Attribute("LOCTYPE", "URL"));
                });

        //TXTGRP
        Element fileGrpTxt = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpTxt.addAttribute(new Attribute("ID", "TXTGRP"));
        fileGrpTxt.addAttribute(new Attribute("USE", "Text"));
        fileInfos.stream()
                .filter(fileInfo -> fileInfo.getCategory() == FileInfo.Category.TXT)
                .filter(fileInfo -> fileInfo.getPageNumber() != null)
                .sorted(Comparator.comparing(FileInfo::getPageNumber))
                .forEach(fileInfo -> {
                    Element fileEl = addNewMetsEl(fileGrpTxt, "file");
                    fileEl.addAttribute(new Attribute("ID", "txt_" + packageUuid + "_" + Utils.to4CharNumber(fileInfo.getPageNumber())));
                    fileEl.addAttribute(new Attribute("SEQ", (fileInfo.getPageNumber() - 1) + ""));
                    fileEl.addAttribute(new Attribute("MIMETYPE", "text/plain"));
                    fileEl.addAttribute(new Attribute("SIZE", fileInfo.getFileSizeBytes() + ""));
                    fileEl.addAttribute(new Attribute("CREATED", nowFormatted));
                    fileEl.addAttribute(new Attribute("CHECKSUM", fileInfo.getMd5Checksum()));
                    fileEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
                    Element fLocatEl = addNewMetsEl(fileEl, "FLocat");
                    fLocatEl.addAttribute(new Attribute("xlink:href", NS_XLINK, fileInfo.getPathFromNdkPackageRoot(false)));
                    fLocatEl.addAttribute(new Attribute("LOCTYPE", "URL"));
                });

        //TECHMDGRP
        Element fileGrpTechMd = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpTechMd.addAttribute(new Attribute("ID", "TECHMDGRP"));
        fileGrpTechMd.addAttribute(new Attribute("USE", "Technical Metadata"));
        fileInfos.stream()
                .filter(fileInfo -> fileInfo.getCategory() == FileInfo.Category.AMDSEC)
                .filter(fileInfo -> fileInfo.getPageNumber() != null)
                .sorted(Comparator.comparing(FileInfo::getPageNumber))
                .forEach(fileInfo -> {
                    Element fileEl = addNewMetsEl(fileGrpTechMd, "file");
                    fileEl.addAttribute(new Attribute("ID", "amd_sec_" + packageUuid + "_" + Utils.to4CharNumber(fileInfo.getPageNumber())));
                    fileEl.addAttribute(new Attribute("SEQ", (fileInfo.getPageNumber() - 1) + ""));
                    fileEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
                    fileEl.addAttribute(new Attribute("SIZE", fileInfo.getFileSizeBytes() + ""));
                    fileEl.addAttribute(new Attribute("CREATED", nowFormatted));
                    fileEl.addAttribute(new Attribute("CHECKSUM", fileInfo.getMd5Checksum()));
                    fileEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
                    Element fLocatEl = addNewMetsEl(fileEl, "FLocat");
                    fLocatEl.addAttribute(new Attribute("xlink:href", NS_XLINK, fileInfo.getPathFromNdkPackageRoot(false)));
                    fLocatEl.addAttribute(new Attribute("LOCTYPE", "URL"));
                });
    }

    private void appendStructMapPhysical(Element rootEl, String monographTitle, List<NamedPage> pages) {
        Element structMapEl = addNewMetsEl(rootEl, "structMap");
        structMapEl.addAttribute(new Attribute("TYPE", "PHYSICAL"));
        structMapEl.addAttribute(new Attribute("LABEL", "Physical_Structure"));

        Element volDivEl = addNewMetsEl(structMapEl, "div");
        volDivEl.addAttribute(new Attribute("ID", "DIV_P_0000"));
        volDivEl.addAttribute(new Attribute("TYPE", "MONOGRAPH"));
        volDivEl.addAttribute(new Attribute("DMDID", "MODSMD_VOLUME_0001"));
        volDivEl.addAttribute(new Attribute("LABEL", monographTitle));

        for (NamedPage page : pages) {
            Element pageDivEl = addNewMetsEl(volDivEl, "div");
            pageDivEl.addAttribute(new Attribute("ID", "DIV_P_PAGE_" + Utils.to4CharNumber(page.getPosition())));
            String pageType = page.getPosition() == 1 ? "titlePage" : "normalPage";
            pageDivEl.addAttribute(new Attribute("TYPE", pageType));
            pageDivEl.addAttribute(new Attribute("ORDER", page.getPosition() + ""));
            pageDivEl.addAttribute(new Attribute("ORDERLABEL", "[" + page.getName() + "]"));
            addNewMetsEl(pageDivEl, "fptr").addAttribute(new Attribute("FILEID", "mc_" + packageUuid + "_" + Utils.to4CharNumber(page.getPosition())));
            addNewMetsEl(pageDivEl, "fptr").addAttribute(new Attribute("FILEID", "uc_" + packageUuid + "_" + Utils.to4CharNumber(page.getPosition())));
            addNewMetsEl(pageDivEl, "fptr").addAttribute(new Attribute("FILEID", "txt_" + packageUuid + "_" + Utils.to4CharNumber(page.getPosition())));
            addNewMetsEl(pageDivEl, "fptr").addAttribute(new Attribute("FILEID", "alto_" + packageUuid + "_" + Utils.to4CharNumber(page.getPosition())));
            addNewMetsEl(pageDivEl, "fptr").addAttribute(new Attribute("FILEID", "amd_mets_" + packageUuid + "_" + Utils.to4CharNumber(page.getPosition())));
        }
    }

    private void appendStructMapLogical(Element rootEl, String monographTitle) {
        Element structMapEl = addNewMetsEl(rootEl, "structMap");
        structMapEl.addAttribute(new Attribute("TYPE", "LOGICAL"));
        structMapEl.addAttribute(new Attribute("LABEL", "Logical_Structure"));

        Element monDivEl = addNewMetsEl(structMapEl, "div");
        monDivEl.addAttribute(new Attribute("ID", "MONOGRAPH_0001"));
        monDivEl.addAttribute(new Attribute("TYPE", "MONOGRAPH"));
        monDivEl.addAttribute(new Attribute("LABEL", monographTitle));

        Element volDivEl = addNewMetsEl(monDivEl, "div");
        volDivEl.addAttribute(new Attribute("ID", "VOLUME_0001"));
        volDivEl.addAttribute(new Attribute("TYPE", "VOLUME"));
        volDivEl.addAttribute(new Attribute("DMDID", "MODSMD_VOLUME_0001"));
        volDivEl.addAttribute(new Attribute("LABEL", monographTitle));
    }

    private void appendStructLink(Element rootEl, List<NamedPage> pages) {
        Element structLinkEl = addNewMetsEl(rootEl, "structLink");
        for (NamedPage page : pages) {
            Element smLinkEl = addNewMetsEl(structLinkEl, "smLink");
            smLinkEl.addAttribute(new Attribute("xlink:from", NS_XLINK, "VOLUME_0001"));
            smLinkEl.addAttribute(new Attribute("xlink:to", NS_XLINK, "DIV_P_PAGE_" + Utils.to4CharNumber(page.getPosition())));
        }
    }

    private void appendVolumeDmdSecMods(Element rootEl, File modsFile) {
        Document modsDoc = Utils.loadXmlFromFile(modsFile);
        enhanceVolumeMods(modsDoc);
        Element dmdSecEl = addNewMetsEl(rootEl, "dmdSec");
        dmdSecEl.addAttribute(new Attribute("ID", "MODSMD_VOLUME_0001"));
        Element mdWrapEl = addNewMetsEl(dmdSecEl, "mdWrap");
        mdWrapEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
        mdWrapEl.addAttribute(new Attribute("MDTYPE", "MODS"));
        mdWrapEl.addAttribute(new Attribute("MDTYPEVERSION", "3.6"));
        Element xmlDataEl = addNewMetsEl(mdWrapEl, "xmlData");
        Element modsRoot = modsDoc.getRootElement().copy();
        modsRoot.addAttribute(new Attribute("ID", "MODS_VOLUME_0001"));
        xmlDataEl.appendChild(modsRoot);
    }

    private void enhanceVolumeMods(Document modsDoc) {
        //append uuid
        Element identifier = addNewModsEl(modsDoc.getRootElement(), "identifier");
        identifier.addAttribute(new Attribute("type", "uuid"));
        identifier.appendChild(packageUuid.toString());
    }

    private void appendVolumeDmdSecDc(Element rootEl, File modsDoc) {
        Document dcDoc = Utils.loadXmlFromFile(modsDoc);
        enhanceVolumeDc(dcDoc);
        Element dmdSecEl = addNewMetsEl(rootEl, "dmdSec");
        dmdSecEl.addAttribute(new Attribute("ID", "DCMD_VOLUME_0001"));
        Element mdWrapEl = addNewMetsEl(dmdSecEl, "mdWrap");
        mdWrapEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
        mdWrapEl.addAttribute(new Attribute("MDTYPE", "DC"));
        Element xmlDataEl = addNewMetsEl(mdWrapEl, "xmlData");
        Element dcRoot = dcDoc.getRootElement().copy();
        xmlDataEl.appendChild(dcRoot);
    }

    private void enhanceVolumeDc(Document dcDoc) {
        //append uuid
        Element identifierEl = addNewDcEl(dcDoc.getRootElement(), "identifier");
        identifierEl.appendChild("uuid:" + packageUuid);
    }

    private void appendPageDmdSecDC(Element rootEl, NamedPage page) {
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

    private void appendPageDmdSecMODS(Element rootEl, NamedPage page) {
        Element dmdSecEl = addNewMetsEl(rootEl, "dmdSec");
        dmdSecEl.addAttribute(new Attribute("ID", "MODSMD_PAGE_" + Utils.to4CharNumber(page.getPosition())));
        Element mdWrapEl = addNewMetsEl(dmdSecEl, "mdWrap");
        mdWrapEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
        mdWrapEl.addAttribute(new Attribute("MDTYPE", "MODS"));
        mdWrapEl.addAttribute(new Attribute("MDTYPEVERSION", "3.6"));
        Element xmlDataEl = addNewMetsEl(mdWrapEl, "xmlData");
        Element modsEl = addNewModsEl(xmlDataEl, "mods");
        modsEl.addAttribute(new Attribute("ID", "MODS_PAGE_" + Utils.to4CharNumber(page.getPosition())));
        //genre
        Element genreEl = addNewModsEl(modsEl, "genre");
        String pageType = page.getPosition() == 1 ? "titlePage" : "normalPage";
        genreEl.addAttribute(new Attribute("type", pageType));
        genreEl.appendChild("page");
        //identifier
        Element identifierEl = addNewModsEl(modsEl, "identifier");
        identifierEl.addAttribute(new Attribute("type", "uuid"));
        identifierEl.appendChild(page.getUuid().toString());
        //part (1)
        Element partEl1 = addNewModsEl(modsEl, "part");
        partEl1.addAttribute(new Attribute("type", pageType));
        Element part1DetailEl = addNewModsEl(partEl1, "detail");
        part1DetailEl.addAttribute(new Attribute("type", "pageNumber"));
        Element partDetailNumber = addNewModsEl(part1DetailEl, "number");
        partDetailNumber.appendChild("[" + page.getName() + "]");
        Element part1ExtentEl = addNewModsEl(partEl1, "extent");
        part1ExtentEl.addAttribute(new Attribute("unit", "pages"));
        Element part1ExtentStartEl = addNewModsEl(part1ExtentEl, "start");
        part1ExtentStartEl.appendChild("[" + page.getName() + "]");
        //part (2)
        Element partEl2 = addNewModsEl(modsEl, "part");
        Element part2DetailEl = addNewModsEl(partEl2, "detail");
        part2DetailEl.addAttribute(new Attribute("type", "pageIndex"));
        Element part2DetailNumberEl = addNewModsEl(part2DetailEl, "number");
        part2DetailNumberEl.appendChild(page.getPosition() + "");
        //recordInfo
        Element recordInfoEl = addNewModsEl(modsEl, "recordInfo");
        Element recordCreationDateEl = addNewModsEl(recordInfoEl, "recordCreationDate");
        recordCreationDateEl.appendChild(nowFormattedIso8601);
        Element recordChangeDateEl = addNewModsEl(recordInfoEl, "recordChangeDate");
        recordChangeDateEl.appendChild(nowFormattedIso8601);
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
        agentCreatorNameEl.appendChild(PACKAGE_CREATOR_CODE);
        //agent ARCHIVIST
        Element agentArchivistEl = addNewMetsEl(metsHdrEl, "agent");
        agentArchivistEl.addAttribute(new Attribute("ROLE", "ARCHIVIST"));
        agentArchivistEl.addAttribute(new Attribute("TYPE", "ORGANIZATION"));
        Element agentArchivistNameEl = addNewMetsEl(agentArchivistEl, "name");
        agentArchivistNameEl.appendChild(PACKAGE_ARCHIVIST_CODE);
    }

    private Element addNewMetsEl(Element parentEl, String elName) {
        return addNewElement(parentEl, "mets:" + elName, NS_METS);
    }

    private Element addNewDcEl(Element parentEl, String elName) {
        return addNewElement(parentEl, "dc:" + elName, NS_DC);
    }

    private Element addNewModsEl(Element parentEl, String elName) {
        return addNewElement(parentEl, "mods:" + elName, NS_MODS);
    }

    private Element addNewElement(Element parentEl, String elName, String namespace) {
        Element element = new Element(elName, namespace);
        if (parentEl != null) {
            parentEl.appendChild(element);
        }
        return element;
    }

}
