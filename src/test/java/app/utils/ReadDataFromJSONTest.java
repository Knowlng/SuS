package app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ReadDataFromJSONTest {
  @Test
  public void testReadPatternDetectorsFromJSON() throws URISyntaxException {
    ReadDataFromJSON readDataFromJSON = new ReadDataFromJSON();

    // Get the path of the JSON file to read from the resources folder
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String filePath =
        Paths.get(classLoader.getResource("json/dangerousPatterns.json").toURI()).toString();

    List<PatternDetector> result = readDataFromJSON.readPatternDetectorsFromJSON(filePath);

    // Check if the returned list is not null and not empty
    assertNotNull(result, "The returned list should not be null.");
    assertFalse(result.isEmpty(), "The list should not be empty.");

    // Check if the first element read from the JSON file is correct
    PatternDetector patternDetector = result.get(0);
    assertEquals(
        "User provided link inside webview",
        patternDetector.getName(),
        "[Test 1]The name of the pattern does not match.");
    assertEquals(
        3, patternDetector.getRequiredState(), "[Test 1] The required state does not match.");
    assertEquals(
        true,
        patternDetector.isDataFlowEnabled(),
        "[Test 1] The value of dataFlow boolean does not match.");
    assertEquals(
        3,
        patternDetector.getDetectors().size(),
        "[Test 1] The number of detectors does not match.");

    // Check if the second element read from the JSON file is correct
    patternDetector = result.get(1);
    assertEquals(
        "Security Risk with JavaScript Interface",
        patternDetector.getName(),
        "[Test 2] The name of the pattern does not match.");
    assertEquals(
        2, patternDetector.getRequiredState(), "[Test 2] The required state does not match.");
    assertEquals(
        false,
        patternDetector.isDataFlowEnabled(),
        "[Test 2] The value of dataFlow boolean does not match.");
    assertEquals(
        2,
        patternDetector.getDetectors().size(),
        "[Test 2] The number of detectors does not match.");
  }

  @Test
  public void testGetBlacklistedIPs() {
    // Call the method to test
    HashMap<String, String> blacklistedIps = ReadDataFromJSON.getBlacklistedIPs();

    // Check if the map is not null
    assertNotNull(blacklistedIps, "The hashmap of blacklisted IPs should not be null.");

    // Check if the map is not empty
    assertFalse(blacklistedIps.isEmpty(), "The map of blacklisted IPs should not be empty.");

    // Optionally, check for a known IP and its corresponding value
    // This assumes you know the content of your test JSON file
    assertTrue(
        blacklistedIps.containsKey("54.81.217.13"), "The Hashmap should contain the known IP.");
    assertEquals(
        "wholesomeserver.media.mit.edu",
        blacklistedIps.get("18.27.197.252"),
        "The node for the known IP does not match.");
  }
}
