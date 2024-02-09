package app.database.databaseInsert;

import app.components.model.DangerousPattern;
import app.components.model.ExpComponent;
import app.components.model.FileInfo;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import app.components.parsing.ParsingProcess;
import app.components.parsing.javaparsing.codeparsing.CodeParser;
import app.components.ui.CommandUI;
import app.database.databaseConnection.DatabaseConnection;
import app.database.databaseDelete.DatabaseDeleteScan;
import app.utils.DataFlowGraphGenerator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsertAnalysisResults {

  public static void insertResults() {
    if (!CommandUI.getHashExist()) {
      insertHashes(CommandUI.getFileHash(), CommandUI.getApkPath());
    } else {
      // Delete all previous code parsing results assosiated with this hash and update
      // hash date scanned
      DatabaseDeleteScan.deleteLastHashResult(CommandUI.getFileHash());
      updateDateCreated(CommandUI.getFileHash());
    }

    // It will insert all detected patterns, foundObjectData and edges
    insertDetectedPatterns(CodeParser.getDetectedPatterns(), CommandUI.getFileHash());

    insertIPAddresses(CodeParser.getFileInfoList(), CommandUI.getFileHash());
    insertXMLDangers(ParsingProcess.getXMLInfoObject(), CommandUI.getFileHash());
    insertPermissionReference(
        ParsingProcess.getXMLInfoObject().getPermissionItems(), CommandUI.getFileHash());
    insertExportedComponents(
        ParsingProcess.getXMLInfoObject().getExpComponents(), CommandUI.getFileHash());
  }

  public static void updateDateCreated(String hashValue) {
    final String updateQuery =
        "UPDATE hashes SET DateAdded = CURRENT_TIMESTAMP WHERE hashvalue = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

      preparedStatement.setString(1, hashValue);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.out.println("Error occurred while updating DateCreated: " + e.getMessage());
    }
  }

  private static void insertHashes(String hash, String basename) {
    final String insertQuery = "INSERT INTO hashes (hashvalue, applicationname) VALUES (?, ?)";

    Path path = Paths.get(basename);
    String fileNameWithExtension = path.getFileName().toString();
    String fileNameWithoutExtension =
        fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

      preparedStatement.setString(1, hash);
      preparedStatement.setString(2, fileNameWithoutExtension);

      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertFoundObjectData(
      DataFlowGraphGenerator dataFlowGraphGenerator, int detectedPatternId) {
    final String insertQuery =
        "INSERT INTO foundobjectdata (objecttype, objectname, detectedpatternid) VALUES (?, ?, ?)";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

      for (String method : dataFlowGraphGenerator.getMethods()) {
        preparedStatement.setString(1, "method");
        preparedStatement.setString(2, method);
        preparedStatement.setInt(3, detectedPatternId);

        preparedStatement.executeUpdate();
      }

      for (String variable : dataFlowGraphGenerator.getVariables()) {
        preparedStatement.setString(1, "variable");
        preparedStatement.setString(2, variable);
        preparedStatement.setInt(3, detectedPatternId);

        preparedStatement.executeUpdate();
      }

    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertEdges(List<String> edges, int detectedPatternId) {
    final String insertQuery =
        "INSERT INTO edge (fromedge, toedge, detectedpatternid) VALUES (?, ?, ?)";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

      for (String edge : edges) {
        String[] parts = edge.split(" -> ");
        String from = parts[0];
        String to = parts[1].split(" ")[0];
        preparedStatement.setString(1, from);
        preparedStatement.setString(2, to);
        preparedStatement.setInt(3, detectedPatternId);

        preparedStatement.executeUpdate();
      }

    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertDetectedPatterns(
      List<DangerousPattern> dangerousPatterns, String hash) {
    final String insertQuery =
        "INSERT INTO detectedPatterns (codesnippet, hashvalue, patternid) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement =
            connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

      for (DangerousPattern pattern : dangerousPatterns) {
        // Check if patterID exist in PatternDetector, because if it does not exist it
        // means
        // PatternDetector was created usign json and not from database
        // we are not storing patterns found from custom json into a database
        if (pattern.getPatternId() == null) {
          continue;
        }

        preparedStatement.setString(1, pattern.getCodeSnippet());
        preparedStatement.setString(2, hash);
        preparedStatement.setInt(3, pattern.getPatternId());

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
          try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
            if (rs.next()) {
              int dangerousPatternId = rs.getInt(1);
              /*
               * Once detectedPattern is inserted succesfully, we can insert foundObjectData
               * and edges
               */
              // Check if dataFlow is enabled, is so insert foundObjectData and edges into
              // database
              if (pattern.isDataFlowEnabled()) {
                insertFoundObjectData(pattern.getDataFlowGraphGenerator(), dangerousPatternId);
                insertEdges(pattern.getDataFlowGraphGenerator().getEdges(), dangerousPatternId);
              }
            }
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertIPAddresses(List<FileInfo> fileInfoList, String hash) {
    final String insertQuery =
        "INSERT INTO ipaddresses (addressvalue, addresstype) VALUES (?, ?) ON CONFLICT (addressvalue) DO NOTHING";
    final String insertRelationQuery =
        "INSERT INTO hashes_ipaddresses (hashvalue, addressvalue) VALUES (?, ?) ON CONFLICT DO NOTHING";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        PreparedStatement relationPreparedStatement =
            connection.prepareStatement(insertRelationQuery)) {

      for (FileInfo fileInfo : fileInfoList) {
        for (String ipv4Address : fileInfo.getIpv4Addresses()) {
          preparedStatement.setString(1, ipv4Address);
          preparedStatement.setString(2, "ipv4");

          int rowsAffected = preparedStatement.executeUpdate();

          if (rowsAffected > 0) {
            relationPreparedStatement.setString(1, hash);
            relationPreparedStatement.setString(2, ipv4Address);
            relationPreparedStatement.executeUpdate();
          }
        }
        for (String ipv6Address : fileInfo.getIpv6Addresses()) {
          preparedStatement.setString(1, ipv6Address);
          preparedStatement.setString(2, "ipv6");

          int rowsAffected = preparedStatement.executeUpdate();

          if (rowsAffected > 0) {
            relationPreparedStatement.setString(1, hash);
            relationPreparedStatement.setString(2, ipv6Address);
            relationPreparedStatement.executeUpdate();
          }
        }
        for (String domain : fileInfo.getDomains()) {
          preparedStatement.setString(1, domain);
          preparedStatement.setString(2, "domain");

          int rowsAffected = preparedStatement.executeUpdate();

          if (rowsAffected > 0) {
            relationPreparedStatement.setString(1, hash);
            relationPreparedStatement.setString(2, domain);
            relationPreparedStatement.executeUpdate();
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertXMLDangers(XMLFileInfo xmlFileInfo, String hash) {
    final String insertRelationQuery =
        "INSERT INTO hashes_xmlpatterns (hashvalue, patternname) VALUES (?, ?) ON CONFLICT DO NOTHING";

    Map<String, Boolean> patterns = new LinkedHashMap<>();
    patterns.put(XMLFileInfo.DEBUGGABLE_PNAME, xmlFileInfo.isAppDebuggable());
    patterns.put(XMLFileInfo.EXPORTED_PNAME, xmlFileInfo.getExpComponents().size() > 0);
    patterns.put(XMLFileInfo.BACKUP_PNAME, xmlFileInfo.isBackupAllowed());
    patterns.put(XMLFileInfo.URIPROVIDER_PNAME, xmlFileInfo.usesGrantUriProviders());

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertRelationQuery)) {

      for (Map.Entry<String, Boolean> entry : patterns.entrySet()) {
        if (entry.getValue()) {
          preparedStatement.setString(1, hash);
          preparedStatement.setString(2, entry.getKey());

          preparedStatement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertPermissionReference(Set<PermissionItem> permissions, String hash) {
    final String checkPermissionQuery = "SELECT permname FROM permissions WHERE permname = ?";
    final String insertHashPermissionQuery =
        "INSERT INTO hashes_permissions (hashvalue, permname) VALUES (?, ?)";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement checkPermissionPreparedStatement =
            connection.prepareStatement(checkPermissionQuery);
        PreparedStatement hashPermissionPreparedStatement =
            connection.prepareStatement(insertHashPermissionQuery)) {

      for (PermissionItem permission : permissions) {
        checkPermissionPreparedStatement.setString(1, permission.getPermissionName());
        ResultSet resultSet = checkPermissionPreparedStatement.executeQuery();

        if (resultSet.next()) {
          hashPermissionPreparedStatement.setString(1, hash);
          hashPermissionPreparedStatement.setString(2, permission.getPermissionName());
          hashPermissionPreparedStatement.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }

  private static void insertExportedComponents(Set<ExpComponent> exportedComponents, String hash) {
    final String insertQuery =
        "INSERT INTO exportedcomponents (componentname, androidname, hashvalue) VALUES (?, ?, ?) ON CONFLICT (componentname, androidname, hashvalue) DO NOTHING";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

      for (ExpComponent component : exportedComponents) {
        preparedStatement.setString(1, component.getComponentName());
        preparedStatement.setString(2, component.getAndroidName());
        preparedStatement.setString(3, hash);

        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while inserting data: " + e.getMessage());
    }
  }
}
