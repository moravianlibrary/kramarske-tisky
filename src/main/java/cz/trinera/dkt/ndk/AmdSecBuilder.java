package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AmdSecBuilder {

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
    private final Map<String, FileInfo> fileInfoByPath;
    private final String monographTitle;

    public AmdSecBuilder(File ndkPackageDir, UUID packageUuid, Timestamp now, Set<FileInfo> fileInfoSet, String monographTitle) {
        this.ndkPackageDir = ndkPackageDir;
        this.packageUuid = packageUuid;
        this.now = now;
        this.nowFormatted = now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        this.fileInfoByPath = buildFileInfoByPath(fileInfoSet);
        this.monographTitle = monographTitle;
    }

    private Map<String, FileInfo> buildFileInfoByPath(Set<FileInfo> fileInfoSet) {
        Map<String, FileInfo> fileInfoByPath = new java.util.HashMap<>();
        for (FileInfo fileInfo : fileInfoSet) {
            fileInfoByPath.put(normalizePath(fileInfo.getPathFromNdkPackageRoot()), fileInfo);
        }
        return fileInfoByPath;
    }

    /*
     * Normalize path to start with "/". If it already starts with "/", return it unchanged.
     *      "alto/alto_1.xml"  -> "/alto/alto_1.xml"
     *      "/alto/alto_1.xml" -> "/alto/alto_1.xml"
     */
    private String normalizePath(String pathFromNdkPackageRoot) {
        if (pathFromNdkPackageRoot.startsWith("/")) {
            return pathFromNdkPackageRoot;
        } else {
            return "/" + pathFromNdkPackageRoot;
        }
    }

    public void buildAndSavePage(int pageNumber) {
        Document doc = buildAmdSec(pageNumber);
        File amdSecDir = new File(ndkPackageDir, "amdsec");
        amdSecDir.mkdirs();
        Utils.saveDocumentToFile(doc, new File(amdSecDir, "amdSec_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".xml"));
    }

    private Document buildAmdSec(int pageNumber) {
        Element rootEl = new Element("mets", "http://www.loc.gov/METS/");
        rootEl.addAttribute(new Attribute("LABEL", monographTitle));
        rootEl.addAttribute(new Attribute("TYPE", "Monograph"));
        appendMetsHdr(rootEl);
        appendAmdSec(rootEl, pageNumber);
        appendFileSec(rootEl, pageNumber);
        appendStructMap(rootEl, pageNumber);
        return new Document(rootEl);
    }

    private void appendStructMap(Element parentEl, int pageNumber) {
        Element structMapEl = addNewMetsEl(parentEl, "structMap");
        structMapEl.addAttribute(new Attribute("TYPE", "PHYSICAL"));
        Element divEl = addNewMetsEl(structMapEl, "div");
        divEl.addAttribute(new Attribute("TYPE", "MONOGRAPH_PAGE"));
        String pageId = Utils.to4CharNumber(pageNumber);
        addNewMetsEl(divEl, "fptr").addAttribute(new Attribute("FILEID", "mc_" + packageUuid + "_" + pageId));
        addNewMetsEl(divEl, "fptr").addAttribute(new Attribute("FILEID", "uc_" + packageUuid + "_" + pageId));
        addNewMetsEl(divEl, "fptr").addAttribute(new Attribute("FILEID", "alto_" + packageUuid + "_" + pageId));
        addNewMetsEl(divEl, "fptr").addAttribute(new Attribute("FILEID", "txt_" + packageUuid + "_" + pageId));
    }

    private void appendFileSec(Element parentEl, int pageNumber) {
        Element fileSecEl = addNewMetsEl(parentEl, "fileSec");
        fileSecEl.addNamespaceDeclaration("xlink", NS_XLINK);

        //fileGrp (ID=MC_IMGGRP)
        Element fileGrpMcEl = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpMcEl.addAttribute(new Attribute("ID", "MC_IMGGRP"));
        fileGrpMcEl.addAttribute(new Attribute("USE", "Images"));
        String mcFilePath = "mastercopy/mc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".jp2";
        FileInfo mcFileInfo = fileInfoByPath.get(normalizePath(mcFilePath));
        Element fileMcEl = addNewMetsEl(fileGrpMcEl, "file");
        fileMcEl.addAttribute(new Attribute("CREATED", nowFormatted));
        fileMcEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
        fileMcEl.addAttribute(new Attribute("CHECKSUM", mcFileInfo.getMd5Checksum()));
        fileMcEl.addAttribute(new Attribute("SIZE", mcFileInfo.getFileSizeBytes().toString()));
        fileMcEl.addAttribute(new Attribute("SEQ", Integer.toString(pageNumber - 1)));
        fileMcEl.addAttribute(new Attribute("ID", "mc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber)));
        fileMcEl.addAttribute(new Attribute("MIMETYPE", "image/jp2"));
        //fileMcEl.addAttribute(new Attribute("ADMID", "MIX_002 OBJ_002 EVT_002")); //TODO
        Element fLocatMcEl = addNewMetsEl(fileMcEl, "FLocat");
        fLocatMcEl.addAttribute(new Attribute("LOCTYPE", "URL"));
        fileMcEl.addAttribute(new Attribute("xlink:href", NS_XLINK, mcFilePath));

        //fileGrp (ID=UC_IMGGRP)
        Element fileGrpUcEl = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpUcEl.addAttribute(new Attribute("ID", "UC_IMGGRP"));
        fileGrpUcEl.addAttribute(new Attribute("USE", "Images"));
        String ucFilePath = "usercopy/uc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".jp2";
        FileInfo ucFileInfo = fileInfoByPath.get(normalizePath(ucFilePath));
        Element fileUcEl = addNewMetsEl(fileGrpUcEl, "file");
        fileUcEl.addAttribute(new Attribute("CREATED", nowFormatted));
        fileUcEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
        fileUcEl.addAttribute(new Attribute("CHECKSUM", ucFileInfo.getMd5Checksum()));
        fileUcEl.addAttribute(new Attribute("SIZE", ucFileInfo.getFileSizeBytes().toString()));
        fileUcEl.addAttribute(new Attribute("SEQ", Integer.toString(pageNumber - 1)));
        fileUcEl.addAttribute(new Attribute("ID", "uc_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber)));
        fileUcEl.addAttribute(new Attribute("MIMETYPE", "image/jp2"));
        Element fLocatUcEl = addNewMetsEl(fileUcEl, "FLocat");
        fLocatUcEl.addAttribute(new Attribute("LOCTYPE", "URL"));
        fLocatUcEl.addAttribute(new Attribute("xlink:href", NS_XLINK, ucFilePath));

        //fileGrp (ID=ALTOGRP)
        Element fileGrpAltoEl = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpAltoEl.addAttribute(new Attribute("ID", "ALTOGRP"));
        fileGrpAltoEl.addAttribute(new Attribute("USE", "Layout"));
        String altoFilePath = "alto/alto_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".xml";
        FileInfo altoFileInfo = fileInfoByPath.get(normalizePath(altoFilePath));
        Element fileAltoEl = addNewMetsEl(fileGrpAltoEl, "file");
        fileAltoEl.addAttribute(new Attribute("CREATED", nowFormatted));
        fileAltoEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
        fileAltoEl.addAttribute(new Attribute("CHECKSUM", altoFileInfo.getMd5Checksum()));
        fileAltoEl.addAttribute(new Attribute("SIZE", altoFileInfo.getFileSizeBytes().toString()));
        fileAltoEl.addAttribute(new Attribute("SEQ", Integer.toString(pageNumber - 1)));
        fileAltoEl.addAttribute(new Attribute("ID", "alto_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber)));
        fileAltoEl.addAttribute(new Attribute("MIMETYPE", "text/xml"));
        //fileGrpAltoEl.addAttribute(new Attribute("ADMID", "OBJ_003 EVT_003")); //TODO
        Element fLocatAltoEl = addNewMetsEl(fileAltoEl, "FLocat");
        fLocatAltoEl.addAttribute(new Attribute("LOCTYPE", "URL"));
        fileAltoEl.addAttribute(new Attribute("xlink:href", NS_XLINK, altoFilePath));

        //fileGrp (ID=TXTGRP)
        Element fileGrpTxtEl = addNewMetsEl(fileSecEl, "fileGrp");
        fileGrpTxtEl.addAttribute(new Attribute("ID", "TXTGRP"));
        fileGrpTxtEl.addAttribute(new Attribute("USE", "Text"));
        String txtFilePath = "txt/txt_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber) + ".txt";
        FileInfo txtFileInfo = fileInfoByPath.get(normalizePath(txtFilePath));
        Element fileTxtEl = addNewMetsEl(fileGrpTxtEl, "file");
        fileTxtEl.addAttribute(new Attribute("CREATED", nowFormatted));
        fileTxtEl.addAttribute(new Attribute("CHECKSUMTYPE", "MD5"));
        fileTxtEl.addAttribute(new Attribute("CHECKSUM", txtFileInfo.getMd5Checksum()));
        fileTxtEl.addAttribute(new Attribute("SIZE", txtFileInfo.getFileSizeBytes().toString()));
        fileTxtEl.addAttribute(new Attribute("SEQ", Integer.toString(pageNumber - 1)));
        fileTxtEl.addAttribute(new Attribute("ID", "txt_" + packageUuid + "_" + Utils.to4CharNumber(pageNumber)));
        fileTxtEl.addAttribute(new Attribute("MIMETYPE", "text/plain"));
        Element fLocatTxtEl = addNewMetsEl(fileTxtEl, "FLocat");
        fLocatTxtEl.addAttribute(new Attribute("LOCTYPE", "URL"));
        fLocatTxtEl.addAttribute(new Attribute("xlink:href", NS_XLINK, txtFilePath));
    }

    private void appendAmdSec(Element parentEl, int pageNumber) {
        Element amdSecEl = addNewMetsEl(parentEl, "amdSec");
        String pageId = Utils.to4CharNumber(pageNumber);
        amdSecEl.addAttribute(new Attribute("ID", "PAGE_" + pageId));
        //TODO: fill amdSec (techMD, digiprovMD)
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
