package app.components.parsing.javaparsing.codeparsing;

import app.components.model.DangerousPattern;
import app.components.model.FileInfo;
import app.components.parsing.javaparsing.addressparsing.FileAnalyzer;
import app.components.parsing.javaparsing.detectors.LiteralExpressionDetector;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detector class for parsing java files and looking for dangerous patterns, ips, domains
 *
 * <p>This class parses the java file, uses FileAnalyzer to extract the ips and domains from the
 * string literals from the java file and then uses Dataflow to find the dangerous patterns in
 * methods
 */
public class CodeParser {

  private static CompilationUnit AST;
  private static int fileCount = 0, fileparsingErrorCount = 0;
  private static List<FileInfo> fileInfoList = new ArrayList<>();
  private static List<DangerousPattern> detectedPatterns = new ArrayList<>();
  private static List<PatternDetector> patternDetectors;

  public static String ParseJavaFiles(List<File> files) throws IOException {
    Map<File, List<String>> fileToStringsMap = new HashMap<>();

    for (File file : files) {
      fileCount++;

      if (files.isEmpty()) {
        throw new FileNotFoundException("Error, there are no java files present");
      }

      try {
        AST = StaticJavaParser.parse(file);
        for (PatternDetector patternDetector : patternDetectors) {
          patternDetector.detect(AST, file);

          if (patternDetector.getDangerousPattern() != null) {
            detectedPatterns.add(patternDetector.getDangerousPattern());
          }
        }

        // Extracting literal expressions from source code
        LiteralExpressionDetector detector = new LiteralExpressionDetector();
        detector.visit(AST, null);
        fileToStringsMap.put(file, detector.getStrings());

      } catch (FileNotFoundException e) {
        fileparsingErrorCount++;
        // System.err.println("File not found: " + file.getAbsolutePath());
      } catch (ParseProblemException e) {
        fileparsingErrorCount++;
        // System.err.println("Failed to parse: " + file.getAbsolutePath());
      }
    }

    try {
      // Collecting domains, ipv4 and ipv6 adresses from java source code string
      // literals
      fileInfoList = FileAnalyzer.processStrings(fileToStringsMap);

    } catch (Exception e) {
      fileparsingErrorCount++;
      System.err.println("An error occurred during file analysis: " + e.getMessage());
    }

    return "\nParsed " + fileCount + " files with " + fileparsingErrorCount + " errors.";
  }

  public static List<FileInfo> getFileInfoList() {
    return fileInfoList;
  }

  public static List<DangerousPattern> getDetectedPatterns() {
    return detectedPatterns;
  }

  public static int getFileCount() {
    return fileCount;
  }

  public static int getFileParsingErrorCount() {
    return fileparsingErrorCount;
  }

  public static void reset() {
    AST = null;
    fileCount = 0;
    fileparsingErrorCount = 0;
    fileInfoList.clear();
    detectedPatterns.clear();
  }

  /**
   * @return A list of {@code PatternDetector} objects which store data about detected dangerous
   *     patterns.
   */
  public static List<PatternDetector> getPatternDetectors() {
    return patternDetectors;
  }

  /**
   * @param patternDetectorsList A list of {@code PatternDetector} objects which store data about
   *     detected dangerous patterns.
   */
  public static void setPatternDetectors(List<PatternDetector> patternDetectorsList) {
    patternDetectors = patternDetectorsList;
  }
}
