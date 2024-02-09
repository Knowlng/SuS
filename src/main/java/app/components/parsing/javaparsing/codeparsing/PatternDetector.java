package app.components.parsing.javaparsing.codeparsing;

import app.components.model.DangerousPattern;
import app.components.parsing.javaparsing.codeparsing.dataflow.DataFlow;
import app.components.parsing.javaparsing.detectors.Detector;
import app.components.parsing.javaparsing.detectors.MethodCallDetector;
import app.utils.DataFlowGraphGenerator;
import app.utils.DotToImage;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the detection of specified pattern within an Abstract Syntax Tree (AST) using a collection
 * of detectors and maintains detection result.
 */
public class PatternDetector {
  private List<Detector> detectors = new ArrayList<>();
  private String patternName;
  private CompilationUnit AST;
  private int currentState = 0, requiredState;
  private boolean isDataFlowEnabled = false;
  private boolean isDataFlowFound = false;
  private MethodCallExpr startExpr;
  private MethodCallExpr endExpr;
  private boolean isPatternFound = false;
  private List<Expression> dataFlowPath = new ArrayList<>();
  private DataFlowGraphGenerator dataFlowGraphGenerator;
  private DangerousPattern dangerousPattern;
  private File currentFile;
  private MethodDeclaration methodDeclaration;
  private String description;
  private String dangerLevel;
  private Integer patternID;
  private List<MethodDeclaration> methodDeclarations = new ArrayList<>();

  public PatternDetector(String patternName, Integer patternID) {
    this.patternName = patternName;
    this.patternID = patternID;
    isPatternFound = true;
  }

  public PatternDetector(
      String patternName,
      int requiredState,
      boolean isDataFlowEnabled,
      String description,
      String dangerLevel) {
    this.patternName = patternName;
    this.requiredState = requiredState;
    this.isDataFlowEnabled = isDataFlowEnabled;
    this.description = description;
    this.dangerLevel = dangerLevel;
  }

  public PatternDetector(
      String patternName,
      int requiredState,
      boolean isDataFlowEnabled,
      String description,
      String dangerLevel,
      Integer patternID) {
    this.patternName = patternName;
    this.requiredState = requiredState;
    this.isDataFlowEnabled = isDataFlowEnabled;
    this.description = description;
    this.dangerLevel = dangerLevel;
    this.patternID = patternID;
  }

  public void execute() {
    // remove all comments from AST
    AST.getAllComments().forEach(comment -> comment.remove());

    String ASTString = "";

    // Get package name from an AST
    String packageName = AST.getPackageDeclaration().get().getNameAsString();
    ASTString += "package " + packageName + "\n\n";

    for (MethodDeclaration methodDeclaration : methodDeclarations) {
      ASTString += methodDeclaration.toString() + "\n";
    }
    methodDeclarations.clear();

    /* Check if advanced dataFlow is enabled */
    if (isDataFlowEnabled) {

      // check if Last detector is not MethodCallDetector
      if (!(detectors.get(detectors.size() - 1) instanceof MethodCallDetector)) {
        System.out.println(
            "Advanced dataFlow is enabled, but Last detector is not MethodCallDetector, dataflow will not be checked between passed methods!");
        printResults();
        dangerousPattern =
            new DangerousPattern(
                patternName,
                ASTString,
                null,
                isDataFlowEnabled,
                currentFile,
                description,
                dangerLevel,
                patternID,
                dataFlowGraphGenerator);
        return;
      }

      // get access to the start and end expressions of the pattern
      startExpr = (MethodCallExpr) detectors.get(0).getDetectedExpression();
      endExpr = (MethodCallExpr) detectors.get(detectors.size() - 1).getDetectedExpression();

      DataFlow dataFlowAnalyzer = new DataFlow();
      isDataFlowFound = dataFlowAnalyzer.checkDataFlowBetweenMethods(AST, startExpr, endExpr, this);

      printResults();
      if (isDataFlowFound) {
        isPatternFound = true;

        String dataflowGraphPath = "";
        try {
          isPatternFound = true;

          dataflowGraphPath = DotToImage.convert(dataFlowGraphGenerator.generateDotGraph());
        } catch (Exception e) {
          System.out.println("Error while converting dataflow graph: " + e.getMessage());
        }

        // convert list of methodDeclarations to string
        String methodDeclarationsString = "";
        methodDeclarationsString += "package " + packageName + "\n\n";
        methodDeclarationsString += methodDeclaration.toString() + "\n";

        dangerousPattern =
            new DangerousPattern(
                patternName,
                methodDeclarationsString,
                dataflowGraphPath,
                isDataFlowEnabled,
                currentFile,
                description,
                dangerLevel,
                patternID,
                dataFlowGraphGenerator);
      }
      printResults();
      return;
    } else {
      printResults();
      isPatternFound = true;
      dangerousPattern =
          new DangerousPattern(
              patternName,
              ASTString,
              null,
              isDataFlowEnabled,
              currentFile,
              description,
              dangerLevel,
              patternID,
              dataFlowGraphGenerator);
      return;
    }
  }

  public void printResults() {
    System.out.println(
        "\n -- Dangerous pattern \""
            + patternName
            + "\" was found in this code snippet using "
            + (isDataFlowFound ? "advanced dataFlow" : "basic")
            + " detector --\n");
    if (isDataFlowFound) {
      System.out.println(
          "\n| DataFlow path between methods \""
              + startExpr.getNameAsString()
              + "\" and \""
              + endExpr.getNameAsString()
              + "\":\n|");

      // Print dataFlow path between methods in functional style
      dataFlowPath.forEach(
          (expr) -> {
            System.out.println("|-> " + expr);
          });
    }

    System.out.println("\n" + AST.toString());
    System.out.println(
        "===================================================================================");
  }

  public boolean isRequiredState() {
    return currentState == requiredState;
  }

  public void incrementState() {
    currentState++;
  }

  public void addDetector(Detector detector) {
    detectors.add(detector);
  }

  public void detect(CompilationUnit AST, File currentFile) {
    this.currentFile = currentFile;
    resetDetectors();
    this.currentState = 0;
    for (Detector detector : detectors) {
      this.AST = AST;
      detector.reset();
      detector.detect(AST);
    }
  }

  public String getName() {
    return patternName;
  }

  public int getRequiredState() {
    return requiredState;
  }

  public List<Detector> getDetectors() {
    return detectors;
  }

  public void resetDetectors() {
    this.currentState = 0;
    this.isDataFlowFound = false;
    dataFlowPath.clear();
    this.dangerousPattern = null;

    for (Detector detector : detectors) {
      detector.reset();
    }
  }

  public boolean isDataFlowEnabled() {
    return isDataFlowEnabled;
  }

  public MethodCallExpr getStartExpr() {
    return startExpr;
  }

  public MethodCallExpr getEndExpr() {
    return endExpr;
  }

  public boolean isPatternFound() {
    return isPatternFound;
  }

  public void updateDataFlowPath(Expression expr) {
    dataFlowPath.add(expr);
  }

  public Integer getPatternID() {
    return patternID;
  }

  public boolean isDataFlowFound() {
    return isDataFlowFound;
  }

  public DataFlowGraphGenerator getDataFlowGraphGenerator() {
    return dataFlowGraphGenerator;
  }

  public void setDataFlowGraphGenerator(DataFlowGraphGenerator dataFlowGraphGenerator) {
    this.dataFlowGraphGenerator = dataFlowGraphGenerator;
  }

  public File getCurrentFile() {
    return currentFile;
  }

  public void setCurrentFile(File currentFile) {
    this.currentFile = currentFile;
  }

  public DangerousPattern getDangerousPattern() {
    return dangerousPattern;
  }

  public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
    this.methodDeclaration = methodDeclaration;
  }

  public Integer getPatternId() {
    return patternID;
  }

  public DataFlowGraphGenerator getDataFlowGraph() {
    return dataFlowGraphGenerator;
  }

  @Override
  public String toString() {
    return "PatternDetector{"
        + "patternName='"
        + patternName
        + '\''
        + ", requiredState="
        + requiredState
        + ", dataFlow="
        + isDataFlowEnabled
        + ", description='"
        + description
        + '\''
        + ", riskLevel='"
        + dangerLevel
        + '\''
        + ", patternID='"
        + patternID
        + '\''
        + '}';
  }

  /**
   * Checks if methodDeclaration already exists in the list of methodDeclarations
   *
   * @param methodDeclaration
   * @return true if methodDeclaration already exists in the list of methodDeclarations
   */
  private boolean isMethodDeclarationExist(MethodDeclaration methodDeclaration) {
    for (MethodDeclaration methodDeclaration1 : methodDeclarations) {
      if (methodDeclaration1.equals(methodDeclaration)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds methodDeclaration to the list of methodDeclarations if it does not exist in the list
   *
   * @param methodDeclaration
   */
  public void addMethodDeclaration(MethodDeclaration methodDeclaration) {
    if (!isMethodDeclarationExist(methodDeclaration)) {
      this.methodDeclarations.add(methodDeclaration);
    }
  }
}
