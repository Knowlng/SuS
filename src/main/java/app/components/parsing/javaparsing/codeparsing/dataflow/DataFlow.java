package app.components.parsing.javaparsing.codeparsing.dataflow;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.detectors.BetweenTwoMethodsDetector;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class DataFlow {
  /**
   * Checks if there is data flow between two methods
   *
   * @param AST Abstract Syntax Tree of the source code
   * @param startExpr The starting expression of the data flow
   * @param endExpr The method call that potentially receives the data
   * @return true if there is data flow between the two methods, false otherwise
   */
  public boolean checkDataFlowBetweenMethods(
      CompilationUnit AST,
      MethodCallExpr startExpr,
      MethodCallExpr endExpr,
      PatternDetector patternDetector) {
    if (AST == null || startExpr == null || endExpr == null) {
      return false;
    }

    // check if endExpr has arguments
    if (endExpr.getArguments().isEmpty()) {
      return false;
    }

    if (isDirectArgument(startExpr, endExpr)) {
      return true;
    }

    /*
     * In this stage endExpr has arguments and non of the arguments are the
     * startExpr. We need to check if there is data flow between the two methods.
     */
    BetweenTwoMethodsDetector dataFlowDetector =
        new BetweenTwoMethodsDetector(startExpr, endExpr, patternDetector, false);

    return dataFlowDetector.checkDataFlow(AST);
  }

  public boolean isDirectArgument(MethodCallExpr startExpr, MethodCallExpr endExpr) {
    for (Expression arg : endExpr.getArguments()) {
      if (arg.equals(startExpr)) {
        return true;
      }
    }
    return false;
  }
}
