package app.patterndetection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.components.parsing.javaparsing.codeparsing.CodeParser;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.utils.ReadDataFromJSON;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PatternDetectionIT {
  private static final String SQL_INJECTION = "SQL Injection";
  private static final String COMMAND_INJECTION = "Command Injection";
  private static final int EXPECTED_PATTERN_COUNT = 19;
  private static final int EXPECTED_FILE_COUNT = 2;
  private static final int EXPECTED_ERROR_COUNT = 0;

  private File sqlInjectionExampleFile;
  private File commandInjectionExampleFile;
  private List<PatternDetector> patternDetectors;

  @BeforeEach
  void setUp() throws IOException {
    sqlInjectionExampleFile =
        Paths.get("src/integrationTest/resources/java/SQLInjectionExample.java").toFile();
    commandInjectionExampleFile =
        Paths.get("src/integrationTest/resources/java/CommandInjectionExample.java").toFile();
    assertTrue(sqlInjectionExampleFile.exists());
    assertTrue(sqlInjectionExampleFile.canRead());
    assertTrue(commandInjectionExampleFile.exists());
    assertTrue(commandInjectionExampleFile.canRead());

    String jsonFilePath = "src/integrationTest/resources/json/patterns.json";
    ReadDataFromJSON reader = new ReadDataFromJSON();
    patternDetectors = reader.readPatternDetectorsFromJSON(jsonFilePath);
  }

  @Test
  void testPatternDetectorsLoadedCorrectly() {
    assertFalse(patternDetectors.isEmpty());
    assertEquals(EXPECTED_PATTERN_COUNT, patternDetectors.size());

    assertEquals(SQL_INJECTION, patternDetectors.get(0).getName());
    assertEquals(COMMAND_INJECTION, patternDetectors.get(1).getName());
  }

  @Test
  void testCodeParserDoesNotThrowExceptions() {
    List<File> javaSourceFiles =
        Arrays.asList(sqlInjectionExampleFile, commandInjectionExampleFile);
    CodeParser.setPatternDetectors(patternDetectors);
    assertDoesNotThrow(() -> CodeParser.ParseJavaFiles(javaSourceFiles));
  }

  @Test
  void testCodeParsing() {
    // Assert that the number of files parsed is 2
    assertEquals(EXPECTED_FILE_COUNT, CodeParser.getFileCount());

    // Assert that the number of files that failed to parse is 0
    assertEquals(EXPECTED_ERROR_COUNT, CodeParser.getFileParsingErrorCount());
  }

  @AfterAll
  static void tearDown() {
    CodeParser.reset();
  }
}
