package app.components.parsing;

import app.components.conversion.ConversionProcess;
import app.components.conversion.classconverter.ClassConverter;
import app.components.model.FileInfo;
import app.components.model.XMLFileInfo;
import app.components.parsing.javaparsing.codeparsing.CodeParser;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.detectors.IpDetector;
import app.components.parsing.xmlparsing.RiskAssigner;
import app.components.parsing.xmlparsing.XMLParser;
import app.components.ui.CommandUI;
import app.database.databaseFetch.DatabaseFetchAnalysis;
import app.utils.ReadDataFromJSON;
import app.utils.ReadFilesFromDirectory;
import app.utils.Timer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class handles the parsing of XML files and Java code files, assessing potential risks and
 * extracting relevant data.
 */
public class ParsingProcess {
  private static XMLFileInfo xmlFileInfo = new XMLFileInfo();
  private static IpDetector ipDetector = new IpDetector();
  private static XMLParser xmlParser = new XMLParser(xmlFileInfo);
  private static RiskAssigner riskAssigner = new RiskAssigner();
  private static Timer timer = new Timer();
  private static Optional<List<String>> convertedXmlStrings;
  private static List<File> javaFileList;

  /**
   * Starts the parsing process for XML and Java files located in a specified directory. This method
   * first initializes the parsing of XML files by calling {@code fillXmlStringList}, and then
   * proceeds to parse Java files. It also reads dangerous patterns from a JSON file for pattern
   * detection in the java code analysis phase.
   *
   * @param convertedDir The directory path where the AndroidManifest.xml and Java files are
   *     located. If null, uses paths defined in the conversion process for fetching files.
   * @throws IOException if an I/O error occurs during file reading or parsing.
   */
  public static void startParsing(Path convertedDir) throws IOException {

    System.out.println("\nParsing 'AndroidManifest.xml' files...");
    timer.start();

    fillXmlStringList(convertedDir);

    if (convertedXmlStrings.isPresent() && !convertedXmlStrings.get().isEmpty()) {
      xmlParser.initializeXMLParsing(convertedXmlStrings.get());

      riskAssigner.assignRiskLevel(xmlFileInfo.getPermissionItems());
    } else {
      System.err.println("No converted XML strings available for parsing, skipping...");
    }

    /* Retrieve dangerous patterns from JSON or Database */
    if (CommandUI.getJsonPatternsPath() != null) {
      System.out.println("\nReading dangerous patterns from JSON...");
      List<PatternDetector> patternDetectors =
          new ReadDataFromJSON().readPatternDetectorsFromJSON(CommandUI.getJsonPatternsPath());
      CodeParser.setPatternDetectors(patternDetectors);
    } else {
      System.out.println("\nReading dangerous patterns from Database...");

      List<PatternDetector> patternDetectors = DatabaseFetchAnalysis.fetchPatternsAndDetectors();
      CodeParser.setPatternDetectors(patternDetectors);
    }

    /* Retrieve all java files from the output folder */
    System.out.println("\nParsing java files...\n");
    if (convertedDir == null) {
      javaFileList =
          ReadFilesFromDirectory.getSpecificFilesFromDirectory(
              ConversionProcess.getOutputPath().resolve(ClassConverter.getOutputFolderName()),
              ".java");
    } else {
      javaFileList = ReadFilesFromDirectory.getSpecificFilesFromDirectory(convertedDir, ".java");
    }
    CodeParser.ParseJavaFiles(javaFileList);

    // Checks blacklisted Ips
    for (FileInfo fileInfo : CodeParser.getFileInfoList()) {
      ipDetector.checkFileIps(fileInfo);
    }
    Timer.printDuration("Total parsing and analysis process", timer.stop());
    System.out.println("====================================================");
  }

  /**
   * Fills a list with XML content strings from the specified directory. If the given directory path
   * is not null, indicating that the use chose only the analysis process, it reads all plain text
   * "AndroidManifest.xml" files in the specified directory and adds their content to the list.
   * Otherwise, it retrieves a converted list of XML strings from the {@code ConversionProcess}
   * class.
   *
   * @param convertedDir The directory path where the XML files are located. If null, uses a
   *     converted list of XML strings from the {@code ConversionProcess} class.
   * @throws IOException if an I/O error occurs when accessing the file system.
   */
  private static void fillXmlStringList(Path convertedDir) throws IOException {
    if (convertedDir == null) {
      convertedXmlStrings = ConversionProcess.getConvertedXMLStringList();
    } else {
      List<File> xmlFileList =
          ReadFilesFromDirectory.getSpecificFilesFromDirectory(convertedDir, "AndroidManifest.xml");
      convertedXmlStrings = Optional.of(new ArrayList<>());
      for (File xmlFile : xmlFileList) {
        try {
          String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()));
          convertedXmlStrings.get().add(xmlContent);
        } catch (IOException e) {
          System.err.println(
              "Could not read xml content from:" + xmlFile.toPath() + " skipping...");
        }
      }
    }
  }

  /**
   * @return A {@code XMLFileInfo} object which stores data about parsed XML files.
   */
  public static XMLFileInfo getXMLInfoObject() {
    return xmlFileInfo;
  }
}
