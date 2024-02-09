package app.patterndetection;

import static org.junit.jupiter.api.Assertions.*;

import app.components.model.FileInfo;
import app.components.parsing.javaparsing.addressparsing.FileAnalyzer;
import app.components.parsing.javaparsing.detectors.IpDetector;
import app.components.parsing.javaparsing.detectors.LiteralExpressionDetector;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class IPDetectionIT {

  @Test
  public void testCompleteIPDetectionFlow() {
    File testFile = loadTestFile("src/integrationTest/resources/java/test.java");

    List<String> extractedStrings = extractStringsFromJavaFile(testFile);

    final int expectedNumberOfStrings = 20; // Replace with the actual expected number
    assertEquals(
        expectedNumberOfStrings,
        extractedStrings.size(),
        "Number of extracted strings does not match expected value");

    Map<File, List<String>> fileToStringsMap = createFileToStringsMap(testFile, extractedStrings);

    List<FileInfo> fileInfoList = processFileStrings(fileToStringsMap);

    assertFileInfoList(fileInfoList);

    checkForBlacklistedIPs(fileInfoList);
  }

  private File loadTestFile(String filePath) {
    File testFile = Paths.get(filePath).toFile();
    assertTrue(testFile.exists(), "Test file does not exist at the specified path");
    return testFile;
  }

  private List<String> extractStringsFromJavaFile(File file) {
    try {
      CompilationUnit AST = StaticJavaParser.parse(file);
      LiteralExpressionDetector detector = new LiteralExpressionDetector();
      detector.visit(AST, null);
      List<String> extractedStrings = detector.getStrings();

      assertNotNull(extractedStrings, "Extracted strings list should not be null");
      assertFalse(extractedStrings.isEmpty(), "Extracted strings list should not be empty");

      return extractedStrings;
    } catch (IOException e) {
      fail("IOException occurred while parsing the file: " + e.getMessage());
      return null; // This line will never be reached due to fail above
    }
  }

  private Map<File, List<String>> createFileToStringsMap(File file, List<String> strings) {
    Map<File, List<String>> fileToStringsMap = new HashMap<>();
    fileToStringsMap.put(file, strings);
    return fileToStringsMap;
  }

  private List<FileInfo> processFileStrings(Map<File, List<String>> fileToStringsMap) {
    List<FileInfo> fileInfoList = FileAnalyzer.processStrings(fileToStringsMap);

    assertNotNull(fileInfoList, "The fileInfoList should not be null");
    assertFalse(fileInfoList.isEmpty(), "The fileInfoList should not be empty");

    return fileInfoList;
  }

  private void assertFileInfoList(List<FileInfo> fileInfoList) {
    int validDomainCount = 0;
    int validIPv6Count = 0;
    int validIPv4Count = 0;

    for (FileInfo fileInfo : fileInfoList) {
      validDomainCount += fileInfo.getDomains().size();
      validIPv4Count += fileInfo.getIpv4Addresses().size();
      validIPv6Count += fileInfo.getIpv6Addresses().size();
    }

    assertEquals(4, validDomainCount, "Number of valid domains does not match expected count");
    assertEquals(2, validIPv4Count, "Number of valid IPv4 addresses does not match expected count");
    assertEquals(1, validIPv6Count, "Number of valid IPv6 addresses does not match expected count");
  }

  private void checkForBlacklistedIPs(List<FileInfo> fileInfoList) {
    IpDetector ipDetector = new IpDetector();
    for (FileInfo fileInfo : fileInfoList) {
      ipDetector.checkFileIps(fileInfo);

      // Assert that blacklisted IPs are identified
      for (String ip : fileInfo.getBlacklistedIPs().keySet()) {
        assertTrue(
            ipDetector.isIpBlacklisted(ip), "Found IP should be in the list of blacklisted IPs");
      }
    }
  }
}
