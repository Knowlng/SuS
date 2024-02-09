package app.components.parsing.javaparsing.detectors;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.codeparsing.dataflow.DataFlowInspector;
import app.utils.DataFlowGraphGenerator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;

/** Checks if there is data flow between two methods */
public class BetweenTwoMethodsDetector extends VoidVisitorAdapter<Void> {

  private MethodCallExpr startExpr, endExpr;
  private final List<String> variablesOfInterest = new ArrayList<>();
  private boolean dataFlowFound, debug = false;
  private PatternDetector patternDetector;
  private DataFlowGraphGenerator dataFlowGraphGenerator = new DataFlowGraphGenerator();
  private MethodDeclaration methodDeclaration;

  public BetweenTwoMethodsDetector(
      MethodCallExpr startExpr, MethodCallExpr endExpr, PatternDetector patternDetector) {
    this.startExpr = startExpr;
    this.endExpr = endExpr;
    this.patternDetector = patternDetector;
  }

  public BetweenTwoMethodsDetector(
      MethodCallExpr startExpr,
      MethodCallExpr endExpr,
      PatternDetector patternDetector,
      boolean debug) {
    this.startExpr = startExpr;
    this.endExpr = endExpr;
    this.patternDetector = patternDetector;
    this.debug = debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Visits the method declaration and checks if there is data flow between the two methods.
   *
   * @param md The method declaration to be checked
   * @param arg
   */
  @Override
  public void visit(MethodDeclaration md, Void arg) {
    super.visit(md, arg);

    md.findAll(Expression.class)
        .forEach(
            expr -> {
              if (expr instanceof AssignExpr) {
                DataFlowInspector.checkAssignExpressionDataFlow(
                        (AssignExpr) expr, startExpr, variablesOfInterest, dataFlowGraphGenerator)
                    .ifPresent(
                        (expression) -> {
                          variablesOfInterest.add(expression);
                          patternDetector.updateDataFlowPath(expr);
                        });
              } else if (expr instanceof VariableDeclarationExpr) {
                DataFlowInspector.checkVariableDelacartionExpressionDataFlow(
                        (VariableDeclarationExpr) expr,
                        startExpr,
                        variablesOfInterest,
                        dataFlowGraphGenerator)
                    .ifPresent(
                        (expression) -> {
                          variablesOfInterest.add(expression);
                          patternDetector.updateDataFlowPath(expr);
                        });
              } else if (expr instanceof MethodCallExpr) {
                if (DataFlowInspector.checkDataFlow(
                    (MethodCallExpr) expr, endExpr, variablesOfInterest, dataFlowGraphGenerator)) {
                  dataFlowFound = true;
                  patternDetector.updateDataFlowPath(expr);
                }
              }
              if (debug) {
                System.out.println(
                    "Current iteration variablesOfInterest: " + variablesOfInterest.toString());
              }
              if (dataFlowFound) {
                // save the method declaration in which the data flow was found
                methodDeclaration = md;
                return;
              }
            });
  }

  /**
   * Checks if there is data flow between the two methods.
   *
   * @param AST Abstract Syntax Tree of the source code
   * @return true if there is data flow between the two methods, false otherwise
   */
  public boolean checkDataFlow(CompilationUnit AST) {
    this.visit(AST, null);
    if (dataFlowFound) {
      patternDetector.setDataFlowGraphGenerator(dataFlowGraphGenerator);
      patternDetector.setMethodDeclaration(methodDeclaration);
    }
    return dataFlowFound;
  }
}
