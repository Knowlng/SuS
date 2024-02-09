package app.userShowcase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.components.model.FileInfo;
import app.components.model.PermissionItem;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.resultsshowcase.MDGenerator;
import app.components.resultsshowcase.PDFConverter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

public class UserShowcaseIT {

  @Test
  public void testUserShowcase() throws IOException {

    FileInfo fileInfo = new FileInfo();

    File mdTestFile = File.createTempFile("test", ".md");
    MDGenerator mdgenerator = new MDGenerator(mdTestFile.getAbsolutePath());

    List<FileInfo> fileInfoList = new ArrayList<>();
    Set<PermissionItem> permissions = new HashSet<>();
    List<String> patternDetectors = new ArrayList<>();

    PatternDetector patternDetector = new PatternDetector("Testing Pattern", 2, false, "", "");
    patternDetectors.add(patternDetector.getName());

    PermissionItem permission = new PermissionItem("android.permission.ACCESS_WIFI_STATE");
    permission.setPermRiskLevel("Normal");
    permission.setPermDescription("This permission allows the app to view Wi-Fi connections.");

    permissions.add(permission);

    fileInfo.setFileName("testfile.java");
    fileInfo.addDomainName("www.google.com");
    fileInfo.addDomainName("www.twitter.com");
    fileInfo.addIpv4Address("177.13.134.109");
    fileInfo.addIpv4Address("101.35.64.197");
    fileInfo.addIpv6Address("26a4:5ff3:9c92:d8e0:37c1:f75a:32eb:2d3a");
    fileInfo.addIpv6Address("5392:b2e7:9d01:ecaa:f902:485b:911f:421e");
    fileInfoList.add(fileInfo);

    for (FileInfo files : fileInfoList) {

      assertNotNull(files.getDomains(), "The domains should not be null");
      assertNotNull(files.getIpv4Addresses(), "The ipv4 addresses should not be null");
      assertNotNull(files.getIpv6Addresses(), "The ipv6 addresses should not be null");
    }

    mdgenerator.appendPermissionToMd(permissions);
    mdgenerator.appendFileInfo(fileInfoList);

    String content = PDFConverter.readMarkdownFile(mdTestFile);
    assertFalse(content.isEmpty(), "The read content should not be empty.");
    assertTrue(
        content.contains("177.13.134.109"),
        "The content should contain the ip address 177.13.134.109");

    String html = PDFConverter.convertMarkdownToHtml(content);

    assertFalse(html.isEmpty(), "HTML content should not be empty.");
    assertTrue(html.contains("177.13.134.109"), "HTML should contain 177.13.134.109 ip address.");

    File pdfFile = File.createTempFile("test", ".pdf");

    PDFConverter.pdfFileName = "test";
    PDFConverter.convertHtmlToPdf(html, pdfFile.getAbsolutePath());

    // Check if the new PDF file is created and not empty
    assertTrue(pdfFile.exists(), "PDF file should be created.");
    assertTrue(pdfFile.length() > 0, "PDF file should not be empty.");

    // Load the PDF document
    PDDocument document = PDDocument.load(pdfFile);
    PDFTextStripper stripper = new PDFTextStripper();
    String pdfContent = stripper.getText(document);
    document.close(); // Manually close the document

    // Assert that the PDF content contains the specific phrase
    assertTrue(
        pdfContent.contains("177.13.134.109"), "PDF should contain the ip address 177.13.134.109");

    // Delete the temporary MD file when its no longer needed
    boolean deletedMd = mdTestFile.delete();
    assertTrue(deletedMd, "Temporary MD file should be deleted successfully.");
  }
}
