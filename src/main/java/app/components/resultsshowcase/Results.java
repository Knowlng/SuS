package app.components.resultsshowcase;

import app.components.model.DangerousPattern;
import app.components.model.XMLFileInfo;
import app.components.parsing.ParsingProcess;
import app.components.parsing.javaparsing.codeparsing.CodeParser;
import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.ui.CommandUI;
import app.database.databaseFetch.DatabaseFetchResults;
import app.utils.OutputSilencer;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for generating the results of the parsing processes. This class compiles the
 * extracted data into a markdown file, then converts it into a pdf file.
 */
public class Results {
  /**
   * Creates and manages the results of the parsing process. This method generates a markdown
   * document with details about permissions, file information, and detected patterns. It also
   * manages the results PDF creation after the markdown file has been filled and deletes the
   * created temporary markdown file at the end. If user specified the need to fetch database
   * results, instead of parsing results, database data is gathered for markdown generation.
   *
   * @param commandUI The user interface component used to manage PDF output path and user
   *     interactions.
   */
  public static void createResults(CommandUI commandUI) {
    boolean isFetchedDatabaseDataTrue = CommandUI.getIsFetchDatabaseDataTrue();
    if (isFetchedDatabaseDataTrue) {
      String apkHash = CommandUI.getFileHash();
      String apkName = DatabaseFetchResults.fetchAppName(apkHash);

      MDGenerator mdgenerator = new MDGenerator(apkName);
      List<DangerousPattern> dangerousPatternList = new ArrayList<>();
      List<PatternDetector> patternDetectorList = new ArrayList<>();

      DatabaseFetchResults.fetchAllDetectedPatternInfo(
          apkHash, dangerousPatternList, patternDetectorList);
      XMLFileInfo xmlInfo = DatabaseFetchResults.fetchXmlPatterns(apkHash);
      xmlInfo.setPermissionItems(DatabaseFetchResults.fetchPermissions(apkHash));

      mdgenerator.appendPageHeader(apkName);
      mdgenerator.appendCollectedSummary(xmlInfo, dangerousPatternList);
      mdgenerator.appendLevelDescription(xmlInfo);
      mdgenerator.appendPatternDetectionSummary(patternDetectorList, xmlInfo);
      mdgenerator.appendXMLDangers();
      createResultsHelper(xmlInfo, mdgenerator, isFetchedDatabaseDataTrue);
      mdgenerator.appendPermissionToMd(xmlInfo.getPermissionItems());
      mdgenerator.appendFileInfoFromDb(DatabaseFetchResults.fetchIPAddresses(apkHash));
      mdgenerator.appendDetectedPatterns(dangerousPatternList);
      startResultsMdGen(mdgenerator, commandUI);
    } else {
      MDGenerator mdgenerator = new MDGenerator();
      XMLFileInfo xmlInfo = ParsingProcess.getXMLInfoObject();

      mdgenerator.appendPageHeader(null);
      mdgenerator.appendCollectedSummary(xmlInfo, CodeParser.getDetectedPatterns());
      mdgenerator.appendLevelDescription(xmlInfo);
      mdgenerator.appendPatternDetectionSummary(CodeParser.getPatternDetectors(), xmlInfo);
      mdgenerator.appendXMLDangers();
      createResultsHelper(xmlInfo, mdgenerator, isFetchedDatabaseDataTrue);
      mdgenerator.appendPermissionToMd(xmlInfo.getPermissionItems());
      mdgenerator.appendFileInfo(CodeParser.getFileInfoList());
      mdgenerator.appendDetectedPatterns(CodeParser.getDetectedPatterns());

      for (PatternDetector detector : CodeParser.getPatternDetectors()) {
        if (detector.isPatternFound())
          System.out.println("Pattern " + detector.getName() + " was detected!");
      }
      System.out.println("====================================================");
      startResultsMdGen(mdgenerator, commandUI);
    }
  }

  private static void startResultsMdGen(MDGenerator mdgenerator, CommandUI commandUI) {
    mdgenerator.generateMD();
    commandUI.managePDFOutput(mdgenerator.getFilePath());
    mdgenerator.deleteMDFile();
  }

  private static void createResultsHelper(
      XMLFileInfo xmlInfo, MDGenerator mdgenerator, boolean isFetchedDatabaseDataTrue) {
    if (isFetchedDatabaseDataTrue) {
      OutputSilencer.silenceOutput();
    }
    if (!xmlInfo.getExpComponents().isEmpty()) {
      mdgenerator.appendExportedComponents(xmlInfo.getExpComponents());
      System.out.println(XMLFileInfo.EXPORTED_PNAME + " was detected!");
    }
    if (xmlInfo.usesGrantUriProviders()) {
      mdgenerator.appendGrantUriProviderPattern();
      System.out.println(XMLFileInfo.URIPROVIDER_PNAME + " was detected!");
    }
    if (xmlInfo.isAppDebuggable()) {
      mdgenerator.appendAndroidDebuggable();
      System.out.println(XMLFileInfo.DEBUGGABLE_PNAME + " was detected!");
    }
    if (xmlInfo.isBackupAllowed()) {
      mdgenerator.appendAndroidAllowBackup();
      System.out.println(XMLFileInfo.BACKUP_PNAME + " was detected!");
    }

    if (isFetchedDatabaseDataTrue) {
      OutputSilencer.restoreOutput();
    }
  }
}
