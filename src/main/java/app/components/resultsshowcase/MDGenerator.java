package app.components.resultsshowcase;

import app.components.conversion.ConversionProcess;
import app.components.model.DangerousPattern;
import app.components.model.ExpComponent;
import app.components.model.FileInfo;
import app.components.model.PermissionItem;
import app.components.model.XMLFileInfo;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.ui.CommandUI;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for creating an md file which stores the analysis results
 *
 * <p>This class provides static methods to create an md file, and then store the analysis results
 * in it.
 */
public class MDGenerator {

  private String filePath;
  private String baseName;

  public MDGenerator() {
    createMDFilepath();
  }

  public MDGenerator(String baseName) {
    this.baseName = baseName;
    this.filePath = System.getProperty("java.io.tmpdir") + File.separator + baseName + ".md";
  }

  /**
   * Creates a Markdown file path based on the APK file path or the analysis directory path. This
   * method determines the base name for the Markdown file depending on whether the APK file path is
   * available or not. If the APK file path is available, it uses the name of the APK file (minus
   * the file extension) as the base name. If the APK file path is not available, it uses the name
   * of the analysis directory. The resulting Markdown file path is formed by appending ".md" to the
   * base name and placing it in the appropriate directory.
   */
  private void createMDFilepath() {

    String apkFilePath = CommandUI.getApkPath();

    if (apkFilePath == null) {
      Path dirFilePath = CommandUI.analysisDirPath();
      this.baseName = dirFilePath.getFileName().toString();
      this.filePath = dirFilePath + File.separator + baseName + ".md";
    } else {
      File apkFile = new File(apkFilePath);
      String apkFileName = apkFile.getName();
      int lastDotIndex = apkFileName.lastIndexOf(".");
      this.baseName = apkFileName.substring(0, lastDotIndex);
      this.filePath = ConversionProcess.getOutputPath() + File.separator + baseName + ".md";
    }
  }

  // Method to check if the file exists, and create it if it doesn't
  public void generateMD() {
    if (!Files.exists(Paths.get(filePath))) {
      try {
        PrintWriter writer = new PrintWriter(filePath, "UTF-8");
        writer.close();
      } catch (IOException e) {
        System.out.println("An error occurred while creating the Markdown file.");
        e.printStackTrace();
      }
    }
  }

  public void appendToMarkdownFile(String content) {
    try (FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.println(content);
    } catch (IOException e) {
      System.out.println("An error occurred while writing to the Markdown file.");
      e.printStackTrace();
    }
  }

  public String getFilePath() {
    return filePath;
  }

  public void appendPageHeader(String appName) {
    if (appName == null) {
      appName = baseName;
    }
    this.appendToMarkdownFile("# **" + appName + " Analysis**\n\n");
  }

  public void appendLevelDescription(XMLFileInfo xmlFileInfo) {
    int perUndefined = 0;
    for (PermissionItem item : xmlFileInfo.getPermissionItems()) {
      if (item.getPermRiskLevel().equals("Undefined")) {
        perUndefined++;
      }
    }
    this.appendToMarkdownFile(
        "<ul>"
            + "<li><span style=\"color: red;\"><strong>High:</strong></span> grants a requesting application access to private user data or control over the device, potentially posing negative impact on the user.</li>\n"
            + "<li><span style=\"color: #FFD800;\"><strong>Normal:</strong></span> default permission level, providing lower-risk access to isolated application-level features with minimal impact on other applications.</li>\n"
            + "<li><span style=\"color: #2177DE;\"><strong>Signature:</strong></span> a permission that the system grants only if the requesting application is signed with the same certificate as the application that declared the permission.</li>\n"
            + (perUndefined > 0
                ? "<li><span style=\"color: black;\"><strong>Undefined:</strong></span> describes custom-made permissions by the application, either not found in our database or lacking a danger level assignment even if present in the database.</li>\n"
                : "")
            + "</ul>");
    this.appendToMarkdownFile(
        "<ul>"
            + "<li><span style=\"color: red;\"><strong>Danger:</strong></span> Patterns marked as \"Danger\" could pose severe risks by exposing data that could be used with malicious intent.</li>\n"
            + "<li><span style=\"color: #FFD800;\"><strong>Medium:</strong></span> Patterns marked as \"Medium\" could expose sensitive information or could pose a medium risk, compromising the system's integrity.</li>\n"
            + "<li><span style=\"color: #4eaa02;\"><strong>Low:</strong></span> Patterns marked as \"Low\" are not considered harmful but could be deemed as poor coding practices or a suboptimal approaches.</li>\n"
            + "</ul>");
  }

  public void appendCollectedSummary(
      XMLFileInfo xmlFileInfo, List<DangerousPattern> dangerousPatternsList) {
    int perHigh = 0,
        perNormal = 0,
        perSignature = 0,
        perUndefined = 0,
        patDanger = 0,
        patMedium = 0,
        patLow = 0;
    for (DangerousPattern pattern : dangerousPatternsList) {
      if (pattern.getDangerLevel().equals("Danger")) {
        patDanger++;
      }
      if (pattern.getDangerLevel().equals("Medium")) {
        patMedium++;
      }
      if (pattern.getDangerLevel().equals("Low")) {
        patLow++;
      }
    }
    if (!xmlFileInfo.getExpComponents().isEmpty()) {
      patDanger++;
    }
    if (xmlFileInfo.usesGrantUriProviders()) {
      patMedium++;
    }
    if (xmlFileInfo.isAppDebuggable()) {
      patDanger++;
    }
    if (xmlFileInfo.isBackupAllowed()) {
      patMedium++;
    }
    for (PermissionItem item : xmlFileInfo.getPermissionItems()) {
      if (item.getPermRiskLevel().equals("High")) {
        perHigh++;
      }
      if (item.getPermRiskLevel().equals("Normal")) {
        perNormal++;
      }
      if (item.getPermRiskLevel().equals("Signature")) {
        perSignature++;
      }
      if (item.getPermRiskLevel().equals("Undefined")) {
        perUndefined++;
      }
    }

    this.appendToMarkdownFile(
        "<table>\n"
            + "<tr>\n"
            + "<th>Permissions found: <span id=\"permissions-count\"></span></th>\n"
            + "<th>Dangerous patterns found: <span id=\"patterns-count\"></span></th>\n"
            + "</tr>\n"
            + "<tr>\n"
            + "<td><span id=\"permissions-high\" style=\"color: red\">High</span>: <strong>"
            + perHigh
            + "</strong></td>\n"
            + "<td><span id=\"patterns-danger\" style=\"color: red\">Danger</span>: <strong>"
            + patDanger
            + "</strong></td>\n"
            + "</tr>\n"
            + "<tr>\n"
            + "<td><span id=\"permissions-normal\" style=\"color: #FFD800\">Normal</span>: <strong>"
            + perNormal
            + "</strong></td>\n"
            + "<td><span id=\"patterns-medium\" style=\"color: #FFD800\">Normal</span>: <strong>"
            + patMedium
            + "</strong></td>\n"
            + "</tr>\n"
            + "<tr>\n"
            + "<td><span id=\"permissions-signature\" style=\"color:#2177DE\">Signature</span>: <strong>"
            + perSignature
            + "</strong></td>\n"
            + "<td><span id=\"patterns-Low\" style=\"color: #4eaa02\">Low</span>: <strong>"
            + patLow
            + "</strong></td>\n"
            + "</tr>\n"
            + (perUndefined != 0
                ? "<tr>\n"
                    + "<td><span id=\"permissions-undefined\">Undefined: </span><strong>"
                    + perUndefined
                    + "</strong></td>\n"
                    + "<td></td>\n"
                    + "</tr>\n"
                : "")
            + "</table>");
  }

  public void appendPermissionToMd(Set<PermissionItem> permissionItems) {
    if (permissionItems.isEmpty()) {
      return;
    }
    this.appendToMarkdownFile("\n<div class=\"pageBreak\"></div>\n");
    this.appendToMarkdownFile("<h2> APK permissions </h2>\n");
    this.appendToMarkdownFile(
        "<p> App permissions help support user privacy by protecting access to the following:</p>\n"
            + "<ul>\n"
            + "<li><b>Restricted data</b>, such as system state and users' contact information </li>\n"
            + "<li><b>Restricted actions</b>, such as connecting to a paired device and recording audio</li>\n"
            + "</ul>");
    this.appendToMarkdownFile(
        "<table>\n"
            + "    <tr>\n"
            + "      <th>Permissions name</th>\n"
            + "      <th>Danger level</th>\n"
            + "      <th>Description</th>\n"
            + "    </tr>");

    for (PermissionItem permission : permissionItems) {
      String riskLevel = permission.getPermRiskLevel();
      String color;
      switch (riskLevel) {
        case "High":
          color = "red";
          break;
        case "Normal":
          color = "#FFD800";
          break;
        case "Signature":
          color = "#4eaa02";
          break;
        default:
          color = "black";
      }

      this.appendToMarkdownFile("<tr>\n");
      this.appendToMarkdownFile("<td>" + permission.getPermissionName() + "</td>\n");
      this.appendToMarkdownFile("<td style='color: " + color + ";'>" + riskLevel + "</td>\n");
      this.appendToMarkdownFile("<td>" + permission.getPermDescription() + "</td>\n");
      this.appendToMarkdownFile("</tr>\n");
    }
    this.appendToMarkdownFile("</table>\n\n");
  }

  public void appendXMLDangers() {
    this.appendToMarkdownFile("\n<div class=\"pageBreak\"></div>\n");
    this.appendToMarkdownFile("<h2> Detected potential XML file dangers: </h2>");
  }

  public void appendExportedComponents(Set<ExpComponent> expComponents) {
    this.appendToMarkdownFile("<h3><strong>" + XMLFileInfo.EXPORTED_PNAME + "</strong></h3>\n");
    this.appendToMarkdownFile(
        "<strong>Risk level: </strong> <span style=\"color: red;\">High</span>\n");
    this.appendToMarkdownFile(
        "The mobile application contains exported activities that can be invoked by other applications residing on device, including malicious ones, to trigger a legitimate application activity in order to perform potentially sensitive actions.\n");
    this.appendToMarkdownFile("#### List of exported components used by the application:\n");
    for (ExpComponent component : expComponents) {
      this.appendToMarkdownFile("- Component Name: " + component.getAndroidName() + "\n");
      this.appendToMarkdownFile("- Package name: " + component.getComponentName() + "\n");
      this.appendToMarkdownFile(
          "-------------------------------------------------------------------------------------------------------");
    }
  }

  public void appendGrantUriProviderPattern() {
    this.appendToMarkdownFile("\n<div class=\"avoidBreak\">\n");
    this.appendToMarkdownFile("<h3>" + XMLFileInfo.URIPROVIDER_PNAME + "</h3>\n");
    this.appendToMarkdownFile(
        "<strong>Risk level: </strong> <span style=\"color: #FFD800\">Normal</span>\n");
    this.appendToMarkdownFile(
        "<p>If a Content Provider manages sensitive data, setting android:grantUriPermissions=\"true\" can lead to unintentional data exposure. Malicious apps might exploit this to access data they normally wouldn't have permissions for.</p>\n");
    this.appendToMarkdownFile("</div>\n");
  }

  public void appendAndroidDebuggable() {
    this.appendToMarkdownFile("\n<div class=\"avoidBreak\">\n");
    this.appendToMarkdownFile("<h3>" + XMLFileInfo.DEBUGGABLE_PNAME + "</h3>\n");
    this.appendToMarkdownFile(
        "<strong>Risk level: </strong> <span style=\"color: red\">High</span>\n");
    this.appendToMarkdownFile(
        "<p>When an application has android:debuggable property set to true, attackers could have more access to the application than intended. It could lead to an attacker being able to attach a debugger, potentially accessing sensitive data, reverse-engineering the app's logic, or injecting malicious code.</p>\n");
    this.appendToMarkdownFile("</div>\n");
  }

  public void appendAndroidAllowBackup() {
    this.appendToMarkdownFile("\n<div class=\"avoidBreak\">\n");
    this.appendToMarkdownFile("<h3>" + XMLFileInfo.BACKUP_PNAME + "</h3>\n");
    this.appendToMarkdownFile(
        "<strong>Risk level: </strong> <span style=\"color: #FFD800\">Normal</span>\n");
    this.appendToMarkdownFile(
        "<p>Enabling the android:allowBackup property allows users to back up application data onto local storage or cloud storage. While this feature is convenient for data recovery, it potentially allows attackers to access the application's data through backup files. This might lead to unauthorized access to sensitive information stored within the app, such as user credentials, personal data, or app settings.</p>\n");
    this.appendToMarkdownFile("</div>\n");
  }

  public void appendFileInfo(List<FileInfo> fileInfoList) {
    this.appendToMarkdownFile("\n");
    this.appendToMarkdownFile("<h2 style='display: inline;'> IP Addresses and domains: </h2>\n");
    if (fileInfoList.isEmpty()) {
      this.appendToMarkdownFile("\nNo IP adresses or domains found\n");
    } else {
      for (FileInfo fileInfo : fileInfoList) {
        this.appendToMarkdownFile(fileInfo.toString());
        this.appendToMarkdownFile(
            "-------------------------------------------------------------------------------------------------------");
      }
    }
    this.appendToMarkdownFile("\n");
  }

  public void appendFileInfoFromDb(Optional<FileInfo> fileInfoObject) {
    this.appendToMarkdownFile("\n<div class=\"pageBreak\"></div>\n");
    this.appendToMarkdownFile("\n ## **IP Addresses and domains:**\n");
    if (fileInfoObject.isEmpty()) {
      this.appendToMarkdownFile("No IP adresses or domains found");
    } else {
      FileInfo fileInfo = fileInfoObject.get();
      if (!fileInfo.getIpv4Addresses().isEmpty()) {
        this.appendToMarkdownFile("#### IPv4 Addresses:\n");
        for (String ipv4 : fileInfo.getIpv4Addresses()) {
          this.appendToMarkdownFile("\n - " + ipv4 + "\n");
        }
      }
      if (!fileInfo.getDomains().isEmpty()) {
        this.appendToMarkdownFile("\n #### Domains:\n");
        for (String domain : fileInfo.getDomains()) {
          this.appendToMarkdownFile("\n - " + domain + "\n");
        }
      }
      if (!fileInfo.getIpv6Addresses().isEmpty()) {
        this.appendToMarkdownFile("\n #### IPv6 Addresses:\n");
        for (String ipv6 : fileInfo.getIpv6Addresses()) {
          this.appendToMarkdownFile("\n - " + ipv6 + "\n");
        }
      }
    }
  }

  public void appendDetectedPatterns(List<DangerousPattern> detectedPatterns) {
    this.appendToMarkdownFile("\n<div class=\"pageBreak\"></div>\n");
    this.appendToMarkdownFile("## Detected potential JAVA file dangers:");

    boolean isFirstPattern = true;
    if (!detectedPatterns.isEmpty()) {
      for (DangerousPattern pattern : detectedPatterns) {
        String dangerLevel = pattern.getDangerLevel();
        String color;
        switch (dangerLevel) {
          case "Danger":
            color = "red";
            break;
          case "Medium":
            color = "#FFD800";
            break;
          case "Low":
            color = "#4eaa02";
            break;
          default:
            color = "black";
            break;
        }
        if (!isFirstPattern) {
          this.appendToMarkdownFile("<div class=\"pageBreak\"></div>\n");
        }
        this.appendToMarkdownFile("<h3> Pattern: " + pattern.getPatternName() + "</h3>\n");
        this.appendToMarkdownFile(
            "<h4 style=\"display: inline;\">Danger level:</h4> <span style=\"color: "
                + color
                + ";\">"
                + dangerLevel
                + "</span>\n");
        if (pattern.getJavaFile() != null) {
          this.appendToMarkdownFile("<p class=\"filePath\">\n");
          this.appendToMarkdownFile("<b>Found in file:</b> " + pattern.getJavaFile() + "\n");
          this.appendToMarkdownFile("</p>\n");
        }
        this.appendToMarkdownFile("#### Description: ");
        this.appendToMarkdownFile(pattern.getDescription() + "\n");
        this.appendToMarkdownFile("#### Code snippet:\n");
        this.appendToMarkdownFile("```java\n");
        this.appendToMarkdownFile(pattern.getCodeSnippet());
        this.appendToMarkdownFile("\n```\n");

        isFirstPattern = false;
        if (pattern.isDataFlowEnabled) {
          this.appendToMarkdownFile("#### Dataflow graph:\n");
          this.appendImage("Dataflow graph", pattern.getDataflowGraphPath());
        }
      }
    } else {
      this.appendToMarkdownFile("\n ## **No patterns were detected**\n");
    }
  }

  public void appendPatternDetectionSummary(
      List<PatternDetector> patternDetectors, XMLFileInfo xmlInfo) {
    this.appendToMarkdownFile(
        "<table>\r\n" + "<tr>\r\n" + "    <th>Detected Patterns</th>\r\n" + "</tr>\r\n");

    for (PatternDetector patternDetector : patternDetectors) {
      if (patternDetector.isPatternFound()) {
        this.appendToMarkdownFile(
            "<tr>\r\n" + "<td>" + patternDetector.getName() + "</td>\r\n" + "</tr>\r\n");
      }
    }

    if (!xmlInfo.getExpComponents().isEmpty()) {
      this.appendToMarkdownFile(
          "<tr>\r\n" + "<td>" + XMLFileInfo.EXPORTED_PNAME + "</td>\r\n" + "</tr>");
    }
    if (!xmlInfo.usesGrantUriProviders()) {
      this.appendToMarkdownFile(
          "<tr>\r\n" + "<td>" + XMLFileInfo.URIPROVIDER_PNAME + "</td>\n" + "</tr>");
    }
    if (xmlInfo.isAppDebuggable()) {
      this.appendToMarkdownFile(
          "<tr>\r\n" + "<td>" + XMLFileInfo.DEBUGGABLE_PNAME + "</td>\n" + "</tr>");
    }
    if (xmlInfo.isBackupAllowed()) {
      this.appendToMarkdownFile(
          "<tr>\r\n" + "<td>" + XMLFileInfo.BACKUP_PNAME + "</td>\n" + "</tr>");
    }

    this.appendToMarkdownFile("</table>\r\n");
  }

  public void appendImage(String imageName, String imageFilePath) {
    if (imageFilePath == null) {
      this.appendToMarkdownFile("\nPlease install `GraphViz` for dataflow graph generation\n");
      return;
    }
    imageFilePath = imageFilePath.replace("\\", "/");
    this.appendToMarkdownFile("![" + imageName + "](file:///" + imageFilePath + ")\n");
  }

  public void deleteMDFile() {
    try {
      File file = new File(filePath);
      if (file.exists()) {
        file.delete();
      } else {
        throw new IOException("The md file was not found");
      }
    } catch (IOException e) {
      System.err.println("Error:" + e.getMessage());
    }
  }
}
