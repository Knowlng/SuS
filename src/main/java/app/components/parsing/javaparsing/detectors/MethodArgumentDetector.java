package app.components.parsing.javaparsing.detectors;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.util.stream.Stream;

/**
 * A specialized detector class for method calls with specific arguments within an Abstract Syntax
 * Tree (AST).
 */
public class MethodArgumentDetector extends MethodCallDetector {
  private final String argumentPattern;
  private final Boolean exactMatch;

  public MethodArgumentDetector(
      String methodToDetect,
      String argumentPattern,
      Boolean exactMatch,
      PatternDetector patternDetector) {
    super(methodToDetect, patternDetector);
    this.argumentPattern = argumentPattern;
    this.exactMatch = exactMatch;
  }

  /**
   * Handles the detected method call expression. If the method has argument of interest of, it
   * calls the super method to handle it. Otherwise, it continues traversing AST(Abstract syntax
   * tree).
   *
   * @param expr the detected method call expression
   */
  @Override
  protected void handleDetectedMethod(MethodCallExpr expr) {
    if (isArgumentDetected(expr)) {
      super.handleDetectedMethod(expr);
    }
  }

  /**
   * Checks if the method call expression has an argument of interest.
   *
   * @param expr the detected method call expression
   * @return true if the method call expression has an argument of interest, false otherwise
   */
  private boolean isArgumentDetected(MethodCallExpr expr) {
    Stream<Expression> arguments = expr.getArguments().stream();

    // Get all the arguments that are string literals
    Stream<String> stringArguments =
        arguments
            .filter(Expression::isStringLiteralExpr)
            .map(argument -> argument.asStringLiteralExpr().asString());

    /*
     * Check if any of the string arguments match the pattern
     * If 'exactMatch' is true, use 'equals', otherwise use 'startsWith'
     */
    boolean isMatchFound =
        stringArguments.anyMatch(
            argumentValue ->
                exactMatch
                    ? argumentValue.equals(argumentPattern)
                    : argumentValue.startsWith(argumentPattern));

    return isMatchFound;
  }

  @Override
  public String toString() {

    String superToString = super.toString();

    return "MethodArgumentDetector{"
        + "super="
        + superToString
        + ", argumentPattern='"
        + argumentPattern
        + '\''
        + ", exactMatch="
        + exactMatch
        + '}';
  }
}
