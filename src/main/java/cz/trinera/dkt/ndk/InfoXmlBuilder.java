package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InfoXmlBuilder {

    private static final String COLLECTION = "Kramářské tisky";
    private static final String INSTITUTION = "MZK";
    private static final String CREATOR = "Trinera";

    public Document build(Timestamp now, UUID packageUuid, Set<FileInfo> fileInfos, File md5File) {
        Element rootEl = new Element("info");
        addElement(rootEl, "created", now.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        addElement(rootEl, "metadataversion", "2.2");
        addElement(rootEl, "packageid", packageUuid.toString());
        addElement(rootEl, "mainmets", "mets_" + packageUuid.toString() + ".xml");
        Element validationEl = addElement(rootEl, "validation", "OK");
        validationEl.addAttribute(new Attribute("version", "dkt-workflow-1.0"));
        Element titleIdEl = addElement(rootEl, "titleid", packageUuid.toString());
        titleIdEl.addAttribute(new Attribute("type", "uuid"));
        addElement(rootEl, "collection", COLLECTION);
        addElement(rootEl, "institution", INSTITUTION);
        addElement(rootEl, "creator", CREATOR);
        addElement(rootEl, "size", computeSize(md5File.getParentFile(), fileInfos).toString());
        addItemList(rootEl, fileInfos);
        Element checksumEl = addElement(rootEl, "checksum", "md5_" + packageUuid.toString() + ".md5");
        checksumEl.addAttribute(new Attribute("type", "md5"));
        checksumEl.addAttribute(new Attribute("checksum", Utils.computeMD5Checksum(md5File)));
        return new Document(rootEl);
    }

    private Long computeSize(File parentFile, Set<FileInfo> fileInfos) {
        AtomicLong totalKilobytes = new AtomicLong();
        fileInfos.stream()
                .filter(fileInfo -> !(fileInfo.getFile().getName().matches("info_.*\\.xml"))) //exclude info file
                .forEach(fileInfo -> totalKilobytes.addAndGet(fileInfo.getFileSizeKilobytes()));
        return totalKilobytes.get();
    }

    private void addItemList(Element rootEl, Set<FileInfo> fileInfos) {
        Element itemListEl = new Element("itemlist");
        rootEl.appendChild(itemListEl);
        itemListEl.addAttribute(new Attribute("itemtotal", Integer.toString(fileInfos.size())));
        fileInfos.stream()
                .sorted((o1, o2) -> o1.getPathFromNdkPackageRoot(false).length() == o2.getPathFromNdkPackageRoot(false).length()
                        ? o1.getPathFromNdkPackageRoot(false).compareTo(o2.getPathFromNdkPackageRoot(false))
                        : Integer.compare(o1.getPathFromNdkPackageRoot(false).length(), o2.getPathFromNdkPackageRoot(false).length()))
                .forEach(fileInfo -> addElement(itemListEl, "item", fileInfo.getPathFromNdkPackageRoot(true)));
    }

    private Element addElement(Element parentElement, String elementName, String elementValue) {
        Element element = new Element(elementName);
        element.appendChild(elementValue);
        parentElement.appendChild(element);
        return element;
    }

}
