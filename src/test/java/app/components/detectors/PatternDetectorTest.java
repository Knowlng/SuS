package app.components.detectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.detectors.MethodArgumentDetector;
import app.components.parsing.javaparsing.detectors.MethodCallDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatternDetectorTest {

  private PatternDetector patternDetector;
  private final String patternName = "TestPattern";
  private final int requiredState = 3;

  @BeforeEach
  public void setUp() {
    /* Create a dangerous Pattern with 3 detectors */
    patternDetector =
        new PatternDetector(
            patternName, requiredState, false, "Description of test pattern", "High");
    patternDetector.addDetector(new MethodCallDetector("getIntent", patternDetector));
    patternDetector.addDetector(
        new MethodArgumentDetector("getQueryParameter", "url", false, patternDetector));
    patternDetector.addDetector(new MethodCallDetector("loadUrl", patternDetector));
  }

  @Test
  public void testGetDetectors() {
    assertEquals(3, patternDetector.getDetectors().size(), "Pattern should have 3 detectors");
  }

  @Test
  public void testAddDetector() {
    // Add detector to already existing pattern with 3 detectors
    patternDetector.addDetector(new MethodCallDetector("getIntent", patternDetector));
    assertEquals(4, patternDetector.getDetectors().size(), "Pattern should have 4 detectors");
  }
}
