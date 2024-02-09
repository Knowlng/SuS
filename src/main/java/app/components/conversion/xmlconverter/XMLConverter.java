package app.components.conversion.xmlconverter;

import AXMLPrinter.Decoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The XMLConverter class provides methods to convert binary AndroidManifest.xml into human-readable
 * xml
 */
public class XMLConverter {

  /**
   * Converts binary XML data from the specified file into a human-readable text XML string.
   *
   * @param binaryXmlFilePath The path to the binary XML file to be converted.
   * @return A text XML string representation of the binary XML data, or null if an error occurs
   *     during the conversion.
   */
  public static String convertToXmlString(String binaryXmlFilePath) {
    try (InputStream inputStream = new FileInputStream(binaryXmlFilePath)) {
      return Decoder.DecodeXML(inputStream);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Converts binary XML InputStream into a human-readable text XML string.
   *
   * @param inputStream Inputstream of the binary XML file to be converted.
   * @return A text XML string representation of the binary XML data, or null if an error occurs
   *     during the conversion.
   */
  public static String convertToXmlString(InputStream inputStream) {
    return Decoder.DecodeXML(inputStream);
  }

  /**
   * Converts binary XML data from the specified file into a human-readable text and writes it to an
   * XML file.
   *
   * @param binaryXmlFilePath The path to the binary XML file to be converted.
   * @param outputXmlFilePath The path to the output XML file where the converted data will be
   *     saved.
   * @return true if the conversion and writing were successful, false otherwise.
   */
  public static boolean convertToXmlFile(String binaryXmlFilePath, String outputXmlFilePath) {
    try (InputStream inputStream = new FileInputStream(binaryXmlFilePath);
        OutputStream outputStream = new FileOutputStream(outputXmlFilePath)) {

      String xmlString = Decoder.DecodeXML(inputStream);
      if (xmlString != null) {
        outputStream.write(xmlString.getBytes());
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Converts binary XML data from the specified InputStream into a human-readable text and writes
   * it to an XML file.
   *
   * @param inputStream Inputstream of the binary XML file to be converted.
   * @param outputXmlFilePath The path to the output XML file where the converted data will be
   *     saved.
   * @return true if the conversion and writing were successful, false otherwise.
   */
  public static boolean convertToXmlFile(InputStream inputStream, String outputXmlFilePath) {
    try (OutputStream outputStream = new FileOutputStream(outputXmlFilePath)) {
      String xmlString = Decoder.DecodeXML(inputStream);
      if (xmlString != null) {
        outputStream.write(xmlString.getBytes());
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
