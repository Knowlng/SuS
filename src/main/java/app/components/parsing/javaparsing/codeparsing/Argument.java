package app.components.parsing.javaparsing.codeparsing;

public class Argument {
  private String argumentName;
  private String argumentType;

  public Argument(String argumentName, String argumentType) {
    this.argumentName = argumentName;
    this.argumentType = argumentType;
  }

  public String getArgumentName() {
    return this.argumentName;
  }

  public void setArgumentName(String argumentName) {
    this.argumentName = argumentName;
  }

  public String getArgumentType() {
    return this.argumentType;
  }

  public void setArgumentType(String argumentType) {
    this.argumentType = argumentType;
  }

  @Override
  public String toString() {
    return getArgumentName() + ":" + getArgumentType();
  }
}
