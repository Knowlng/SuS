package app.patterndetection;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import app.components.parsing.javaparsing.codeparsing.CodeParser;
import app.components.resultsshowcase.PDFConverter;
import app.components.ui.CommandUI;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class EndToEndIT {

  @Test
  void testIfPdfReportIsGeneratedAndNotEmpty(@TempDir Path tempDir) throws FileNotFoundException {

    String apkPath = "src/test/resources/apk/calc.apk";
    String outputDir = tempDir.toString();

    ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
    Scanner scanner = new Scanner(in);
    CommandUI commandUI = new CommandUI(scanner);

    String[] args = {"-mt", "-outpdf", outputDir, apkPath};
    commandUI.manageUserInput(args);

    // Check if the PDF report is generated
    File pdfFile = new File(PDFConverter.getPdfFilePath());
    assertTrue(pdfFile.exists(), "PDF report file should be generated.");

    // Check that the PDF report is not empty
    try (FileInputStream fis = new FileInputStream(pdfFile)) {
      assertTrue(fis.available() > 0, "PDF report file should not be empty.");
    } catch (IOException e) {
      fail("Failed to read the generated PDF file", e);
    }
  }

  @AfterAll
  static void tearDown() {
    CodeParser.reset();
  }
}
