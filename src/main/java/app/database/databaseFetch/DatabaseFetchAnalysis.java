package app.database.databaseFetch;

import app.components.model.PermissionItem;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.detectors.MethodArgumentDetector;
import app.components.parsing.javaparsing.detectors.MethodCallDetector;
import app.components.parsing.javaparsing.detectors.ObjectCreationDetector;
import app.database.databaseConnection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseFetchAnalysis {
  private static final String OBJ_CREATION_DET = "ObjectCreationDetector";
  private static final String METHOD_CALL_DET = "MethodCallDetector";
  private static final String METHOD_ARG_DET = "MethodArgumentDetector";

  private static final String SELECT_PATTERNS_DETECTORS =
      "SELECT p.PatternID, p.PatternName, p.RequiredState, p.DataFlow, p.RiskLevel, p.PatternDesc, "
          + "d.OrderIndex, d.ArgumentPattern, d.MethodName, d.DetectorType, d.ExactMatch "
          + "FROM Patterns p "
          + "INNER JOIN Detector d ON p.PatternID = d.PatternID "
          + "ORDER BY p.PatternID, d.OrderIndex";

  private static final String SELECT_PERMISSIONS_SQL =
      "SELECT PermName, RiskLevel, PermDesc FROM Permissions;";

  private static final String CHECK_DATE_SQL =
      "SELECT EXISTS ("
          + "SELECT 1 FROM Hashes h "
          + "WHERE h.HashValue = ? AND ("
          + "SELECT MAX(t.DateAdded) FROM ("
          + "SELECT DateAdded FROM Hashes "
          + "UNION ALL "
          + "SELECT DateCreated FROM Permissions "
          + "UNION ALL "
          + "SELECT DateCreated FROM XMLPatterns "
          + "UNION ALL "
          + "SELECT DateCreated FROM Patterns "
          + ") AS t) > h.DateAdded)";

  private static final String SELECT_IF_HASH_EXISTS =
      "SELECT COUNT(*) FROM hashes WHERE hashvalue = ?";

  public static List<PatternDetector> fetchPatternsAndDetectors() {
    List<PatternDetector> patternDetectorList = new ArrayList<>();

    try (Connection conn = DatabaseConnection.connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_PATTERNS_DETECTORS)) {

      while (rs.next()) {
        int patternID = rs.getInt("PatternID");
        String patternName = rs.getString("PatternName");
        int requiredState = rs.getInt("RequiredState");
        boolean dataFlow = rs.getBoolean("DataFlow");
        String riskLevel = rs.getString("RiskLevel");
        String patternDesc = rs.getString("PatternDesc");
        String argumentPattern = rs.getString("ArgumentPattern");
        String methodName = rs.getString("MethodName");
        String detectorType = rs.getString("DetectorType");
        boolean exactMatch = rs.getBoolean("ExactMatch");

        PatternDetector existingPattern = null;
        for (PatternDetector pattern : patternDetectorList) {
          if (pattern.getPatternID() == patternID) {
            existingPattern = pattern;
            break;
          }
        }

        PatternDetector pattern;
        if (existingPattern == null) {
          pattern =
              new PatternDetector(
                  patternName, requiredState, dataFlow, patternDesc, riskLevel, patternID);
          patternDetectorList.add(pattern);
        } else {
          pattern = existingPattern;
        }

        switch (detectorType) {
          case OBJ_CREATION_DET:
            pattern.addDetector(new ObjectCreationDetector(methodName, pattern));
            break;
          case METHOD_CALL_DET:
            pattern.addDetector(new MethodCallDetector(methodName, pattern));
            break;
          case METHOD_ARG_DET:
            pattern.addDetector(
                new MethodArgumentDetector(methodName, argumentPattern, exactMatch, pattern));
            break;
          default:
            throw new IllegalArgumentException(
                "\nInvalid detector type " + detectorType + " specified inside the database");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return patternDetectorList;
  }

  public static HashMap<String, PermissionItem> fetchPermissions() {
    HashMap<String, PermissionItem> hm = new HashMap<>();
    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PERMISSIONS_SQL)) {

      ResultSet rs = preparedStatement.executeQuery();

      while (rs.next()) {
        String permName = rs.getString("PermName");
        String riskLevel = rs.getString("RiskLevel");
        String permDesc = rs.getString("PermDesc");

        hm.put(permName, new PermissionItem(permName, riskLevel, permDesc));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return hm;
  }

  public static boolean checkIfHashExist(String hash) {
    boolean hashExists = false;

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_IF_HASH_EXISTS)) {

      preparedStatement.setString(1, hash);

      ResultSet resultSet = preparedStatement.executeQuery();

      // if hash exists in a database, set hashExists to true
      if (resultSet.next()) {
        int count = resultSet.getInt(1);
        if (count > 0) {
          hashExists = true;
        }
      }
    } catch (SQLException e) {

      System.out.println(
          "Error occurred while searching for hash in a database: " + e.getMessage());
    }

    return hashExists;
  }

  public static boolean isHashDateOlder(String hashValue) {
    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(CHECK_DATE_SQL)) {
      preparedStatement.setString(1, hashValue);

      ResultSet rs = preparedStatement.executeQuery();

      if (rs.next()) {
        return rs.getBoolean(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }
}
