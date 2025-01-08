package cz.trinera.dkt.marc21;

import cz.trinera.dkt.Utils;
import nu.xom.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Marc21ToMarcXmlConverter {

    public void convert(File inFile, File outFile) throws IOException {
        String marc21Record = new String(Files.readAllBytes(Paths.get(inFile.toURI())));
        Document marcXml = convert(marc21Record);
        Files.write(Paths.get(outFile.toURI()), marcXml.toXML().getBytes());
    }

    public Document convert(String marc21Record) {
        StringBuilder xml = new StringBuilder();
        String[] lines = marc21Record.split("\n");

        // Start XML structure with namespaces
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<record xmlns=\"http://www.loc.gov/MARC21/slim\"");
        xml.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        xml.append(" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\"");
        xml.append(">\n");

        // Process the leader
        xml.append("  <leader>").append(lines[0].trim()).append("</leader>\n");

        // Process each MARC field
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() < 3) continue; // Skip malformed lines

            String tag = line.substring(0, 3);

            if (tag.matches("00[1-8]")) {
                // Control fields (001-008)
                xml.append("  <controlfield tag=\"").append(tag).append("\">");
                xml.append(line.substring(4).trim());
                xml.append("</controlfield>\n");
            } else {
                // Data fields (everything else)
                xml.append("  <datafield tag=\"").append(tag).append("\" ind1=\"")
                        .append(line.charAt(4)).append("\" ind2=\"").append(line.charAt(5)).append("\">\n");

                String[] subfields = line.substring(7).split("\\$");
                for (String subfield : subfields) {
                    if (subfield.length() > 1) {
                        xml.append("    <subfield code=\"").append(subfield.charAt(0)).append("\">");
                        xml.append(subfield.substring(1).trim());
                        xml.append("</subfield>\n");
                    }
                }
                xml.append("  </datafield>\n");
            }
        }

        // Close XML structure
        xml.append("</record>");

        String xmlString = xml.toString();
        return Utils.loadXmlFromString(xmlString);
    }
}
