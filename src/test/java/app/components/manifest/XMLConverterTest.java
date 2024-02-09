package app.components.manifest;

import static org.junit.jupiter.api.Assertions.*;

import app.components.conversion.xmlconverter.XMLConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class XMLConverterTest {

  @Test
  void testConvertToXmlString() throws IOException {
    // Load the XML file from the resources folder
    InputStream inputStream = getClass().getResourceAsStream("/xml/binxml/AndroidManifest-bin.xml");
    assertNotNull(inputStream, "Checks that the input file exists");

    // Call the method and assert the output
    String xmlString = XMLConverter.convertToXmlString(inputStream);
    assertNotNull(xmlString, "Check that converted XML if not null");

    // Read the content of the existing converted xml file.
    try (InputStream expectedFileStream =
        getClass().getResourceAsStream("/xml/AndroidManifest.xml")) {

      String expectedContent = new String(expectedFileStream.readAllBytes());
      assertEquals(expectedContent, xmlString, "Check if XML content decoded as expected");

    } catch (Exception e) {
      e.printStackTrace();
      fail("An error encountered during the test: " + e.getMessage());
    }
  }

  @Test
  void testConvertToXmlFile() {
    // Define test input and output file paths
    InputStream inputStream = getClass().getResourceAsStream("/xml/binxml/AndroidManifest-bin.xml");
    assertNotNull(inputStream, "Check if input file exists");

    // Get the path to the system's temporary directory
    String tempDir = System.getProperty("java.io.tmpdir");

    // Define the output file path inside the temporary directory
    String outputFilePath = tempDir + File.separator + "AndroidManifest-txt.xml";

    try {

      boolean result = XMLConverter.convertToXmlFile(inputStream, outputFilePath);
      assertTrue(result, "Check if the conversion was successful");

      // Check if the output file exists
      File outputFile = new File(outputFilePath);
      assertTrue(outputFile.exists(), "Check if output file exists");

      // Check if XML content decoded as expected

      // Read the content of the existing converted xml file.
      String expectedContent;
      try (InputStream expectedFileStream =
          getClass().getResourceAsStream("/xml/AndroidManifest.xml")) {
        expectedContent = new String(expectedFileStream.readAllBytes());
      }

      String outputContent;
      try (InputStream expectedFileStream = new FileInputStream(outputFilePath)) {
        outputContent = new String(expectedFileStream.readAllBytes());
      }

      assertEquals(expectedContent, outputContent, "Check if XML content decoded as expected");

      // Clean up: Delete the temporary file
      outputFile.delete();

    } catch (Exception e) {
      e.printStackTrace();
      fail("An error encountered during the test: " + e.getMessage());
    }
  }
}
