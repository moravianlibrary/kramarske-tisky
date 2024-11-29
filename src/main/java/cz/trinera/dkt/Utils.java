package cz.trinera.dkt;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class Utils {

    private static final int BLOCK_SIZE = 8192; // 8 KB

    public static void copyFile(File inFile, File outFile) {
        try (FileInputStream inputStream = new FileInputStream(inFile);
             FileOutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[BLOCK_SIZE];
            int bytesRead;

            // Read and write data in chunks
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Error while copying file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Document loadXmlFromFile(File file) {
        try {
            Builder builder = new Builder();
            return builder.build(file);
        } catch (ParsingException e) {
            System.err.println("Parsing error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void saveDocumentToFile(Document document, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(document.toXML().getBytes());
        } catch (IOException e) {
            System.err.println("Error while saving document to file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Document convertDocumentWithXslt(Document document, File xsltFile) {
        try {
            // Convert XOM Document to W3C DOM Document
            org.w3c.dom.Document w3cDocument = convertXOMToDOM(document);

            // Create a TransformerFactory
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            // Load the XSLT file
            StreamSource xsltSource = new StreamSource(xsltFile);
            Transformer transformer = transformerFactory.newTransformer(xsltSource);

            // Prepare the input and output sources
            DOMSource source = new DOMSource(w3cDocument);  // Input W3C DOM Document
            DOMResult result = new DOMResult();            // Output transformed document

            // Perform the transformation
            transformer.transform(source, result);

            // Return the transformed document (convert DOM to XOM if needed)
            org.w3c.dom.Document transformedDOM = (org.w3c.dom.Document) result.getNode();
            return new nu.xom.Builder().build(new ByteArrayInputStream(toByteArray(transformedDOM)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error during XSLT transformation: " + e.getMessage(), e);
        }
    }

    public static org.w3c.dom.Document convertXOMToDOM(Document xomDocument) {
        try {
            // Serialize XOM Document to a byte stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(outputStream, "UTF-8");
            serializer.write(xomDocument);

            // Parse the serialized XML into a W3C DOM Document
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert XOM Document to W3C DOM Document: " + e.getMessage(), e);
        }
    }

    private static byte[] toByteArray(org.w3c.dom.Document domDocument) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(domDocument), new StreamResult(outputStream));
        return outputStream.toByteArray();
    }

    public static void prettyPrintDocument(Document document, OutputStream outputStream) {
        try {
            // Create a Serializer with the output stream
            Serializer serializer = new Serializer(outputStream, "UTF-8");

            // Enable pretty printing
            serializer.setIndent(4); // Number of spaces for indentation
            serializer.setMaxLength(80); // Max line length (wrap lines)

            // Write the formatted XML
            serializer.write(document);
        } catch (Exception e) {
            throw new RuntimeException("Error while printing indented XML: " + e.getMessage(), e);
        }
    }

    public static void prettyPrintDocument(Document document, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            prettyPrintDocument(document, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error while printing indented XML to file: " + e.getMessage(), e);
        }
    }

    public static String prettyPrintDocument(Document document) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            prettyPrintDocument(document, outputStream);
            return outputStream.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while printing indented XML to string: " + e.getMessage(), e);
        }
    }
}
