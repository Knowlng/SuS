package app.components.model;

import app.utils.DataFlowGraphGenerator;
import java.io.File;

public class DangerousPattern {
  private String patternName;
  private String codeSnippet;
  private String dataflowGraphPath;
  public Boolean isDataFlowEnabled;
  private File javaFile;
  private String description;
  private String dangerLevel;
  private Integer patternId;
  private DataFlowGraphGenerator dataFlowGraphGenerator;

  public DangerousPattern(
      Integer patternId,
      String patternName,
      String codeSnippet,
      String dataflowGraphPath,
      Boolean isDataFlowEnabled,
      String description,
      String dangerLevel) {
    this.patternName = patternName;
    this.codeSnippet = codeSnippet;
    this.dataflowGraphPath = dataflowGraphPath;
    this.isDataFlowEnabled = isDataFlowEnabled;
    this.description = description;
    this.dangerLevel = dangerLevel;
  }

  public DangerousPattern(
      String patternName,
      String codeSnippet,
      String dataflowGraphPath,
      Boolean isDataFlowEnabled,
      File javaFile,
      String description,
      String dangerLevel,
      Integer patternId,
      DataFlowGraphGenerator dataFlowGraphGenerator) {
    this.patternName = patternName;
    this.codeSnippet = codeSnippet;
    this.dataflowGraphPath = dataflowGraphPath;
    this.isDataFlowEnabled = isDataFlowEnabled;
    this.javaFile = javaFile;
    this.description = description;
    this.dangerLevel = dangerLevel;
    this.patternId = patternId;
    this.dataFlowGraphGenerator = dataFlowGraphGenerator;
  }

  public String getPatternName() {
    return patternName;
  }

  public void setPatternName(String patternName) {
    this.patternName = patternName;
  }

  public String getCodeSnippet() {
    return codeSnippet;
  }

  public void setCodeSnippet(String codeSnippet) {
    this.codeSnippet = codeSnippet;
  }

  public String getDataflowGraphPath() {
    return dataflowGraphPath;
  }

  public void setDataflowGraphPath(String dataflowGraphPath) {
    this.dataflowGraphPath = dataflowGraphPath;
  }

  public File getJavaFile() {
    return javaFile;
  }

  public void setJavaFile(File javaFile) {
    this.javaFile = javaFile;
  }

  public String getDescription() {
    return description;
  }

  public String getDangerLevel() {
    return dangerLevel;
  }

  public Integer getPatternId() {
    return patternId;
  }

  public DataFlowGraphGenerator getDataFlowGraphGenerator() {
    return dataFlowGraphGenerator;
  }

  public Boolean isDataFlowEnabled() {
    return isDataFlowEnabled;
  }

  @Override
  public String toString() {
    return "DangerousPattern{"
        + "patternName='"
        + patternName
        + '\''
        + ", codeSnippet='"
        + codeSnippet
        + '\''
        + (isDataFlowEnabled ? ", dataflowGraphPath='" + dataflowGraphPath + '\'' : "")
        + ", isDataFlowEnabled="
        + isDataFlowEnabled
        + (javaFile == null ? "no file" : "javaFile= " + javaFile.getAbsolutePath())
        + ", description="
        + description
        + ", dangerLevel="
        + dangerLevel
        + '}';
  }
}
