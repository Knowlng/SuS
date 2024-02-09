package app.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.components.resultsshowcase.PDFConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PDFConverterTest {

  @Test
  public void readMarkdownFileTest() throws IOException {
    File mdTestFile = Paths.get("src/test/resources/md/test.md").toFile();
    String content = PDFConverter.readMarkdownFile(mdTestFile);

    assertFalse(content.isEmpty(), "The read content should not be empty.");
    assertTrue(content.contains("Cat"), "The content should contain the keyword 'Cat'");
  }

  @Test
  public void testConvertMarkdownToHtml() {
    String markdown =
        "# Sample Markdown\n\n"
            + "This is a *sample* Markdown file to test the `convertMarkdownToHtml` method.\n\n"
            + "## Sub-heading\n\n"
            + "- Item 1\n"
            + "- Item 2\n"
            + "- Item 3\n\n"
            + "`Code snippet here`";

    String html = PDFConverter.convertMarkdownToHtml(markdown);

    assertFalse(html.isEmpty(), "HTML content should not be empty.");
    assertTrue(html.contains("<h1>"), "HTML should contain an <h1> tag for the heading.");
  }

  @Test
  public void testConvertHtmlToPdf(@TempDir Path tempDir) throws IOException {
    String htmlContent = "<html><body><h1>Test Header</h1><p>This is a test.</p></body></html>";

    Path pdfFilePath = tempDir.resolve("test.pdf");

    PDFConverter.convertHtmlToPdf(htmlContent, pdfFilePath.toString());

    File pdfFile = pdfFilePath.toFile();
    assertTrue(pdfFile.exists(), "PDF file should be created.");
    assertTrue(pdfFile.length() > 0, "PDF file should not be empty.");
  }
}
