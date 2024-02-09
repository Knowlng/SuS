package app.database.databaseFetch;

import app.components.model.DangerousPattern;
import app.components.model.ExpComponent;
import app.components.model.FileInfo;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.database.databaseConnection.DatabaseConnection;
import app.utils.DataFlowGraphGenerator;
import app.utils.DotToImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DatabaseFetchResults {
  private static final String FETCH_EXPORTED_COMPONENTS =
      "SELECT ComponentName, AndroidName FROM ExportedComponents WHERE HashValue = ?;";

  private static final String FETCH_IP_ADDRESSES =
      "SELECT IPA.AddressValue, IPA.AddressType FROM Hashes_IPAddresses HIP JOIN IPAddresses IPA ON HIP.AddressValue = IPA.AddressValue WHERE HIP.HashValue = ?;";
  private static final String DOMAIN_TYPE_NAME = "domain";
  private static final String IPV4_TYPE_NAME = "ipv4";
  private static final String IPV6_TYPE_NAME = "ipv6";

  private static final String FETCH_XML_PATTERNS =
      "SELECT PatternName FROM Hashes_XMLPatterns WHERE HashValue = ?;";

  private static final String FETCH_DETECTED_PATTERN_DATA =
      "SELECT dp.DetectedPatternID, dp.CodeSnippet, p.RiskLevel, p.PatternName, p.PatternDesc, p.DataFlow, p.PatternID "
          + "FROM DetectedPatterns dp "
          + "INNER JOIN Hashes h ON dp.HashValue = h.HashValue "
          + "INNER JOIN Patterns p ON dp.PatternID = p.PatternID "
          + "WHERE h.HashValue = ?;";

  private static final String FETCH_OBJECT_INFO =
      "SELECT ObjectType, ObjectName FROM FoundObjectData WHERE DetectedPatternID = ?;";

  private static final String OBJECT_TYPE_METHOD = "method";
  private static final String OBJECT_TYPE_VARIABLE = "variable";

  private static final String FETCH_EDGE_INFO =
      "SELECT FromEdge, ToEdge FROM Edge WHERE DetectedPatternID = ?;";

  private static final String FETCH_APP_NAME =
      "SELECT ApplicationName FROM Hashes WHERE HashValue = ?;";

  private static final String FETCH_PERMISSIONS =
      "SELECT p.PermName, p.RiskLevel, p.PermDesc FROM Hashes_Permissions hp JOIN Permissions p ON hp.PermName = p.PermName WHERE hp.HashValue = ?;";

  public static Set<ExpComponent> fetchExportedComponents(String hashValue) {
    Set<ExpComponent> exportedComponents = new HashSet<>();
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_EXPORTED_COMPONENTS)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String componentName = rs.getString("ComponentName");
        String androidName = rs.getString("AndroidName");
        exportedComponents.add(new ExpComponent(componentName, androidName));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return exportedComponents;
  }

  public static Optional<FileInfo> fetchIPAddresses(String hashValue) {
    FileInfo fileInfo = new FileInfo();
    boolean isEmpty = true;
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_IP_ADDRESSES)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        isEmpty = false;

        String addressValue = rs.getString("AddressValue");
        String addressType = rs.getString("AddressType");

        switch (addressType) {
          case IPV4_TYPE_NAME:
            fileInfo.addIpv4Address(addressValue);
            break;
          case IPV6_TYPE_NAME:
            fileInfo.addIpv6Address(addressType);
            break;
          case DOMAIN_TYPE_NAME:
            fileInfo.addDomainName(addressValue);
            break;
          default:
            throw new IllegalArgumentException(
                "\nInvalid detector address type "
                    + addressType
                    + " specified inside the database");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return isEmpty ? Optional.empty() : Optional.of(fileInfo);
  }

  public static XMLFileInfo fetchXmlPatterns(String hashValue) {
    XMLFileInfo xmlInfo = new XMLFileInfo();
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_XML_PATTERNS)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String patternName = rs.getString("PatternName");
        switch (patternName) {
          case XMLFileInfo.DEBUGGABLE_PNAME:
            xmlInfo.setAppDebuggable(true);
            break;
          case XMLFileInfo.EXPORTED_PNAME:
            // also fetches exported components into XMLFileInfo object if they were marked
            // as found
            xmlInfo.setExpComponents(fetchExportedComponents(hashValue));
            break;
          case XMLFileInfo.URIPROVIDER_PNAME:
            xmlInfo.setUsesGrantUriProviders(true);
            break;
          case XMLFileInfo.BACKUP_PNAME:
            xmlInfo.setBackupAllowedValue(true);
            break;
          default:
            throw new IllegalArgumentException(
                "\nXML danger pattern name "
                    + patternName
                    + "exists in database, but does not match any created ones");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return xmlInfo;
  }

  public static void fetchAllDetectedPatternInfo(
      String hashValue,
      List<DangerousPattern> dangerousPatternList,
      List<PatternDetector> patternDetectorList) {
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_DETECTED_PATTERN_DATA)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Integer patternID = rs.getInt("PatternID");
        Integer detectedPatternID = rs.getInt("DetectedPatternID");
        String codeSnippet = rs.getString("CodeSnippet");
        String riskLevel = rs.getString("RiskLevel");
        String patternName = rs.getString("PatternName");
        String patternDesc = rs.getString("PatternDesc");
        boolean dataFlow = rs.getBoolean("DataFlow");

        DangerousPattern existingDangerousPattern = null;
        for (DangerousPattern dangerousPattern : dangerousPatternList) {
          if (dangerousPattern.getPatternId() == detectedPatternID) {
            existingDangerousPattern = dangerousPattern;
            break;
          }
        }

        if (existingDangerousPattern == null) {
          String dataflowGraphPath;
          if (dataFlow) {
            DataFlowGraphGenerator graphGenerator = new DataFlowGraphGenerator();
            fetchObjectInfo(detectedPatternID, graphGenerator);
            fetchEdgeInfo(detectedPatternID, graphGenerator);
            dataflowGraphPath =
                DotToImage.convert(
                    graphGenerator.generateDotGraph(), System.getProperty("java.io.tmpdir"));
          } else {
            dataflowGraphPath = null;
          }
          DangerousPattern dangerousPattern =
              new DangerousPattern(
                  detectedPatternID,
                  patternName,
                  codeSnippet,
                  dataflowGraphPath,
                  dataFlow,
                  patternDesc,
                  riskLevel);
          dangerousPatternList.add(dangerousPattern);
        }

        PatternDetector existingPattern = null;
        for (PatternDetector pattern : patternDetectorList) {
          if (pattern.getPatternID() == patternID) {
            existingPattern = pattern;
            break;
          }
        }

        if (existingPattern == null) {
          PatternDetector pattern = new PatternDetector(patternName, patternID);
          patternDetectorList.add(pattern);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void fetchObjectInfo(int detectedPatternID, DataFlowGraphGenerator graphGenerator) {
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_OBJECT_INFO)) {

      stmt.setInt(1, detectedPatternID);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String objectType = rs.getString("ObjectType");
        String objectName = rs.getString("ObjectName");

        switch (objectType) {
          case OBJECT_TYPE_METHOD:
            graphGenerator.addMethod(objectName, "");
            break;
          case OBJECT_TYPE_VARIABLE:
            graphGenerator.addVariable(objectName, "");
            break;
          default:
            throw new IllegalArgumentException(
                "\nObject type "
                    + objectType
                    + "exists in database, but does not match any created ones");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void fetchEdgeInfo(int detectedPatternID, DataFlowGraphGenerator graphGenerator) {
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_EDGE_INFO)) {

      stmt.setInt(1, detectedPatternID);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String fromEdge = rs.getString("FromEdge");
        String toEdge = rs.getString("ToEdge");

        graphGenerator.addEdge(fromEdge, toEdge, "");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static String fetchAppName(String hashValue) {
    String applicationName = null;
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_APP_NAME)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        applicationName = rs.getString("ApplicationName");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return applicationName;
  }

  public static Set<PermissionItem> fetchPermissions(String hashValue) {
    Set<PermissionItem> permissions = new HashSet<>();
    try (Connection conn = DatabaseConnection.connect();
        PreparedStatement stmt = conn.prepareStatement(FETCH_PERMISSIONS)) {

      stmt.setString(1, hashValue);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        String permName = rs.getString("PermName");
        String riskLevel = rs.getString("RiskLevel");
        String permDesc = rs.getString("PermDesc");
        permissions.add(new PermissionItem(permName, riskLevel, permDesc));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return permissions;
  }
}
