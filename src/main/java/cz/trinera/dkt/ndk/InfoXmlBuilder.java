package cz.trinera.dkt.ndk;

import cz.trinera.dkt.Utils;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

public class InfoXmlBuilder {

    private static final String COLLECTION = "Kramářské tisky";
    private static final String INSTITUTION = "MZK";
    private static final String CREATOR = "Trinera";

    public Document build(Timestamp now, UUID packageUuid, Set<String> filePaths, File md5File) {
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
        addElement(rootEl, "size", Integer.toString(computeSize(md5File.getParentFile(), filePaths)));
        addItemList(rootEl, filePaths);
        Element checksumEl = addElement(rootEl, "checksum", "md5_" + packageUuid.toString() + ".md5");
        checksumEl.addAttribute(new Attribute("type", "md5"));
        checksumEl.addAttribute(new Attribute("checksum", Utils.computeMD5Checksum(md5File)));
        return new Document(rootEl);
    }

    private int computeSize(File parentFile, Set<String> filePaths) {
        int totalBytes = 0;
        for (String filePath : filePaths) {
            File file = new File(parentFile, filePath);
            if (!file.getName().startsWith("info")) {
                totalBytes += file.length();
            }
        }
        return totalBytes / 1024;
    }

    private void addItemList(Element rootEl, Set<String> filePaths) {
        Element itemListEl = new Element("itemlist");
        rootEl.appendChild(itemListEl);
        itemListEl.addAttribute(new Attribute("itemtotal", Integer.toString(filePaths.size())));
        filePaths.stream()
                .sorted((o1, o2) -> o1.length() == o2.length() ? o1.compareTo(o2) : Integer.compare(o1.length(), o2.length()))
                .forEach(filePath -> addElement(itemListEl, "item", filePath));
    }

    private Element addElement(Element parentElement, String elementName, String elementValue) {
        Element element = new Element(elementName);
        element.appendChild(elementValue);
        parentElement.appendChild(element);
        return element;
    }

}
