package cz.trinera.dkt;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static Document loadXmlFromString(String xmlString) {
        try {
            Builder builder = new Builder();
            return builder.build(new StringReader(xmlString));
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

    public static void saveStringToFile(String content, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content.getBytes());
        } catch (IOException e) {
            System.err.println("Error while saving string to file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Document convertDocumentWithXslt(Document document, File xsltFile) {
        try {
            // Step 1: Convert XOM Document to String
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(outputStream, "UTF-8");
            serializer.setIndent(2);
            serializer.write(document);
            String xmlString = outputStream.toString("UTF-8");

            // Step 2: Prepare XSLT transformer
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsltFile));

            // Step 3: Perform the transformation
            StringReader xmlInput = new StringReader(xmlString);
            StringWriter xmlOutput = new StringWriter();
            transformer.transform(new StreamSource(xmlInput), new StreamResult(xmlOutput));

            // Step 4: Convert the transformed XML back to a XOM Document
            Builder builder = new Builder();
            return builder.build(new StringReader(xmlOutput.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error during XSLT transformation: " + e.getMessage(), e);
        }
    }

    public static boolean validateXmlAgainstXsd(Document xmlDoc, File xsdFile) {
        try {
            // Create a SchemaFactory for the W3C XML Schema language
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the XSD file
            Schema schema = factory.newSchema(xsdFile);

            // Create a Validator instance
            Validator validator = schema.newValidator();

            // Validate the XML string
            validator.validate(new StreamSource(new StringReader(xmlDoc.toXML())));

            // If no exception is thrown, the XML is valid
            return true;
        } catch (IOException | SAXException e) {
            // Handle validation errors
            System.err.println("XML Validation Error: " + e.getMessage());
            return false;
        }
    }

    @Deprecated
    public static Document convertDocumentWithXsltBak(Document document, File xsltFile) {
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

    @Deprecated
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

    public static String to4CharNumber(int number) {
        return String.format("%04d", number);
    }

    public static String computeMD5Checksum(File file) {
        try {
            // Create an instance of MessageDigest with MD5 algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Read the file in chunks
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            // Compute the MD5 hash
            byte[] digest = md.digest();

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Error while computing MD5 checksum: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates deep copy of the directory structure. Throws exception if something goes wrong (access rights, filesystem error, etc)
     *
     * @param inputDir  input directory containing data
     * @param outputDir output directory, probably not existing
     * @throws IOException if an I/O error occurs
     */
    public static void copyDirectory(File inputDir, File outputDir) {
        try {
            if (!inputDir.exists() || !inputDir.isDirectory()) {
                throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + inputDir);
            }
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Failed to create output directory: " + outputDir);
            }
            File[] files = inputDir.listFiles();
            if (files == null) {
                throw new IOException("Failed to list contents of directory: " + inputDir);
            }
            for (File file : files) {
                File destFile = new File(outputDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile); // Recursive copy for subdirectories
                } else {
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the directory and all its contents recursively.
     *
     * @param dir
     */
    public static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file);
                        }
                    }
                }
            }
            if (!dir.delete()) {
                System.err.println("Failed to delete directory: " + dir);
            }
        }
    }
}
