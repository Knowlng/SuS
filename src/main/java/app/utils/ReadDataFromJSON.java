package app.utils;

import app.components.model.PermissionItem;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.detectors.MethodArgumentDetector;
import app.components.parsing.javaparsing.detectors.MethodCallDetector;
import app.components.parsing.javaparsing.detectors.ObjectCreationDetector;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * The {@code ReadDataFromJSON} class provides a utility for reading data from a JSON file and
 * converting it into a HashMap of key-value pairs.
 */
public class ReadDataFromJSON {
  private static final Gson GSON = new Gson();
  private static final String OBJ_CREATION_DET = "ObjectCreationDetector";
  private static final String METHOD_CALL_DET = "MethodCallDetector";
  private static final String METHOD_ARG_DET = "MethodArgumentDetector";

  /**
   * Reads data from a JSON file and converts it into a HashMap.
   *
   * @param fileName The name of the JSON file to read data from.
   * @return A HashMap containing the data read from the JSON file, where keys are strings and
   *     values are strings.
   */
  public HashMap<String, PermissionItem> readPermissionsToHashMap(String fileName) {
    HashMap<String, PermissionItem> hm = new HashMap<>();

    try (Reader reader = new FileReader(fileName)) {

      hm = GSON.fromJson(reader, new TypeToken<HashMap<String, PermissionItem>>() {}.getType());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return hm;
  }

  /**
   * Read dangerous patterns from a JSON file and convert them into a List of PatternDetector
   * objects.
   *
   * @param fileName The name of the JSON file to read data from.
   * @return A List of PatternDetector objects containing dangerous patterns read from the JSON file
   */
  public List<PatternDetector> readPatternDetectorsFromJSON(String filename) {
    List<PatternDetector> patternDetectors = new ArrayList<>();

    try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
      JsonArray jsonPatterns = GSON.fromJson(reader, JsonArray.class);

      for (JsonElement patElem : jsonPatterns) {
        JsonObject pattern = patElem.getAsJsonObject();

        String patternName = getJsonValue(pattern, "name", JsonElement::getAsString, "String");
        int requiredState =
            getJsonValue(pattern, "requiredState", JsonElement::getAsInt, "Integer");
        boolean dataFlow = getJsonValue(pattern, "dataFlow", JsonElement::getAsBoolean, "Boolean");
        String description =
            getJsonValue(pattern, "description", JsonElement::getAsString, "String");
        String dangerLevel =
            getJsonValue(pattern, "dangerLevel", JsonElement::getAsString, "String");

        PatternDetector patternDetector =
            new PatternDetector(patternName, requiredState, dataFlow, description, dangerLevel);

        JsonArray detectors = pattern.getAsJsonArray("detectors");
        for (JsonElement detectorElement : detectors) {
          JsonObject detector = detectorElement.getAsJsonObject();

          String type = getJsonValue(detector, "type", JsonElement::getAsString, "String");
          String methodName =
              getJsonValue(detector, "methodName", JsonElement::getAsString, "String");

          switch (type) {
            case OBJ_CREATION_DET:
              patternDetector.addDetector(new ObjectCreationDetector(methodName, patternDetector));
              break;
            case METHOD_CALL_DET:
              patternDetector.addDetector(new MethodCallDetector(methodName, patternDetector));
              break;
            case METHOD_ARG_DET:
              String argumentPattern =
                  getJsonValue(detector, "argumentPattern", JsonElement::getAsString, "String");

              Boolean exactMatch =
                  getJsonValue(detector, "exactMatch", JsonElement::getAsBoolean, "Boolean");

              patternDetector.addDetector(
                  new MethodArgumentDetector(
                      methodName, argumentPattern, exactMatch, patternDetector));
              break;
            default:
              throw new IllegalArgumentException(
                  "\nInvalid detector type: " + type + " while reading " + filename);
          }
        }
        patternDetectors.add(patternDetector);
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return patternDetectors;
  }

  /**
   * Get a value from a json object. If the field is missing or the value is null and If the value
   * cannot be converted to the desired type, a JsonParseException is thrown.
   *
   * @param <T> type of the value to get
   * @param jsonObject json object to get value from
   * @param field field to get value from
   * @param converter function to convert json element to desired type
   * @param typeName type of the value
   * @return value of the field converted to the desired type using the converter function.
   * @throws JsonParseException if the field is missing or the value is null or the value cannot be
   *     converted to the desired type
   */
  private static <T> T getJsonValue(
      JsonObject jsonObject, String field, Function<JsonElement, T> converter, String typeName) {
    JsonElement element = jsonObject.get(field);

    if (element == null || element.isJsonNull()) {
      throw new JsonParseException("The field '" + field + "' is missing in the JSON object.");
    }

    try {
      return converter.apply(element);
    } catch (NumberFormatException e) {
      throw new JsonParseException(
          "Invalid format for field '"
              + field
              + "'. Expected a number, but got: "
              + element.getAsString());
    } catch (Exception e) {
      throw new JsonParseException(
          "Error processing field '"
              + field
              + "'. Expected type: "
              + typeName
              + ". Error: "
              + e.getMessage(),
          e);
    }
  }

  /**
   * Reads a JSON file containing blacklisted IP addresses and their corresponding nodes, and
   * returns a {@link HashMap} with this data.
   *
   * <p>The method reads the JSON file specified at "src/main/resources/json/blacklistedIps.json".
   * Each entry in the JSON file should represent an IP address and its corresponding node,
   * structured as key-value pairs.
   *
   * @return A {@link HashMap} where each key is a blacklisted IP address and the value is the
   *     corresponding node(Host).
   * @throws IllegalStateException if the JSON file is empty or cannot be properly read into a
   *     HashMap.
   */
  public static HashMap<String, String> getBlacklistedIPs() {
    HashMap<String, String> ipMap = new HashMap<>();
    try (FileReader reader = new FileReader("blacklistedIps.json")) {

      Type type = new TypeToken<HashMap<String, String>>() {}.getType();
      ipMap = GSON.fromJson(reader, type);

      if (ipMap == null || ipMap.isEmpty()) {
        throw new IllegalStateException("Blacklisted IP list is empty.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ipMap;
  }
}
