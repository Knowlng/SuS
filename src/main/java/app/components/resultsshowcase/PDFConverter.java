package app.components.resultsshowcase;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.*;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * This class provides static methods to create a pdf file from an md file by converting the md to
 * html format, and then using the openhtmltopdf library to convert the contents to pdf format
 */
public class PDFConverter {

  public static String pdfFileName;
  private static String pdfFilePath;

  public static void generate(File mdFile, String outputPath)
      throws FileNotFoundException, IllegalArgumentException, IOException {

    if (!mdFile.exists()) {
      throw new FileNotFoundException("Markdown file not found: " + mdFile.getPath());
    }
    // Checks if the file is actually .md format and extracts the base name from md
    // file name, so
    // the file doesn't have an .md extension
    String mdFileName = mdFile.getName();
    int lastDotIndex = mdFileName.lastIndexOf(".");
    String baseName;

    if (lastDotIndex != -1 && mdFileName.substring(lastDotIndex).toLowerCase().equals(".md")) {
      baseName = mdFileName.substring(0, lastDotIndex);
    } else {
      throw new IllegalArgumentException(
          "The provided file does not have a mandatory .md extension: " + mdFileName);
    }
    // Creates a pdf file name with the base name and the extension
    pdfFileName = baseName + ".pdf";
    pdfFilePath = outputPath + File.separator + pdfFileName;

    String markdownContent = readMarkdownFile(mdFile);
    String htmlContent = convertMarkdownToHtml(markdownContent);

    convertHtmlToPdf(htmlContent, pdfFilePath);
  }

  public static String readMarkdownFile(File mdFile) throws FileNotFoundException, IOException {
    StringBuilder contentBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(mdFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        contentBuilder.append(line).append("\n");
      }
    }

    return contentBuilder.toString();
  }

  public static String convertMarkdownToHtml(String markdown) throws IllegalArgumentException {
    Parser parser = Parser.builder().build();
    Node document = parser.parse(markdown);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    String htmlContent = renderer.render(document);

    if (htmlContent.isEmpty()) {
      throw new IllegalArgumentException("Markdown is not being converted into HTML");
    }
    return "<!DOCTYPE html>\n"
        + "<html>\n"
        + "<head>\n"
        + "<title>Document</title>\n"
        + "<style>"
        + "table {"
        + " font-family: Arial, sans-serif;"
        + " border-collapse: collapse;"
        + " width: 100%;"
        + "}"
        + ".pageBreak {"
        + "page-break-after: always;"
        + "}"
        + "td, th {"
        + "border: 1px solid #000000;"
        + "text-align: left;"
        + "padding: 8px;"
        + "}"
        + "ul{"
        + "list-style-type: square;"
        + "}"
        + ".avoidBreak {"
        + "page-break-inside: avoid;"
        + "}"
        + ".filePath {"
        + "word-wrap: break-word;"
        + "}"
        + "img {"
        + "width: 85%;"
        + "height: auto;"
        + "page-break-after: always;"
        + "}"
        + "pre {"
        + "white-space: pre-wrap;"
        + "}"
        + "</style>"
        + "</head>\n"
        + "<body>\n"
        + htmlContent
        + "</body>\n"
        + "</html>";
  }

  public static void convertHtmlToPdf(String html, String pdfFilePath) throws IOException {
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));

    try (OutputStream os = new FileOutputStream(pdfFilePath)) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.withHtmlContent(html, null);
      builder.toStream(os);
      builder.run();
    } catch (IOException e) {
      System.setErr(originalErr);
      throw new IOException("Could not write to PDF: " + e.getMessage());
    } finally {
      System.setErr(originalErr);
    }
  }

  public static String getPdfFileName() {
    return pdfFileName;
  }

  public static String getPdfFilePath() {
    return pdfFilePath;
  }
}
