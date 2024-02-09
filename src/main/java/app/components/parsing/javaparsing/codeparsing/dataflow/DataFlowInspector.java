package app.components.parsing.javaparsing.codeparsing.dataflow;

import app.components.parsing.javaparsing.codeparsing.ExpressionInspector;
import app.utils.DataFlowGraphGenerator;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import java.util.List;
import java.util.Optional;

/**
 * Utility class that provides static methods to inspect dataFlow from expression nodes within the
 * AST (Abstract Syntax Tree).
 */
public class DataFlowInspector {
  /**
   * Checks if the provided {@link AssignExpr} contains any data flow from the provided list of
   * variables or method call expression.
   *
   * @param assignExpr the {@link AssignExpr} to inspect
   * @param startExpr the method call that potentially starts the data flow
   * @param variablesOfInterest the list of variables to check for
   * @return the name of the variable that the result of the expression is assigned to or null if
   *     data flow is not found
   */
  public static Optional<String> checkAssignExpressionDataFlow(
      AssignExpr assignExpr,
      MethodCallExpr startExpr,
      List<String> variablesOfInterest,
      DataFlowGraphGenerator dataFlowGraphGenerator) {
    // Right side of the expression: Variable's name or Method Call Expression
    Expression right = assignExpr.getValue();
    // Left side of the expression: Variable's name
    String left = assignExpr.getTarget().toString();

    if (right instanceof NameExpr) {
      // Check if it self assign (e.g. x = x)
      if (right.toString().equals(left)) {
        return Optional.empty();
      }

      // Check if right side is variable from variablesOfInterest list
      if (variablesOfInterest.contains(right.toString())) {
        /*
         * check if left side is not already inside variablesOfInterest
         * e. g. x = y where y is in variablesOfInterest, but x is also in
         * variablesOfInterest (Check if there is no variable reassignation)
         */
        if (!variablesOfInterest.contains(left)) {
          dataFlowGraphGenerator.addVariable(left, left);
          dataFlowGraphGenerator.addEdge(right.toString(), left, "");
          return Optional.of(left);
        }
        return Optional.empty();
      }
    }
    if (right instanceof MethodCallExpr) {
      /*
       * check if right side is startExpr
       * e.g x = method() where method is startExpr
       */
      if (right.asMethodCallExpr().getNameAsString().equals(startExpr.getNameAsString())) {
        dataFlowGraphGenerator.addMethod(
            startExpr.getNameAsString(), startExpr.getNameAsString() + "()");
        dataFlowGraphGenerator.addVariable(left, left);
        dataFlowGraphGenerator.addEdge(startExpr.getNameAsString(), left, "");
        return Optional.of(left);
      }

      /*
       * Check if method is invoked from a variableOfInterest
       * e.g. x = y.method() where y is in variablesOfInterest
       */
      Optional<String> variableInvokedFrom = ExpressionInspector.getVariableThatInvoked(right);
      if (variableInvokedFrom.isPresent()
          && variablesOfInterest.contains(variableInvokedFrom.get())) {
        /*
         * check if left side is not already inside variablesOfInterest
         * (Check if there is no variable reassignation)
         * e. g. x = x.method() where x is in variablesOfInterest
         */
        if (!variablesOfInterest.contains(left)) {
          dataFlowGraphGenerator.addVariable(left, left);
          dataFlowGraphGenerator.addEdge(variableInvokedFrom.get(), left, "");
          return Optional.of(left);
        }
      }

      /*
       * check if any arguments inside methodCall are variablesOfInterest
       * e.g x = method(y) where y is in variablesOfInterest
       */
      if (ExpressionInspector.checkMethodCallForSpecificArguments(
          right.asMethodCallExpr(), variablesOfInterest)) {
        dataFlowGraphGenerator.addVariable(left, left);
        dataFlowGraphGenerator.addEdge(right.toString(), left, "");
        return Optional.of(left);
      }
    }

    // return Optional.empty() if no data flow found
    return Optional.empty();
  }

  /**
   * Checks if the provided {@link VariableDeclarationExpr} contains any data flow from the provided
   * list of variables or method call expression.
   *
   * @param variableDeclarationExpr the {@link VariableDeclarationExpr} to inspect
   * @param startExpr the method call that potentially starts the data flow
   * @param variablesOfInterest the list of variables to check for
   * @return the name of the variable that the result of the expression is assigned to or null if
   *     data flow is not found
   */
  public static Optional<String> checkVariableDelacartionExpressionDataFlow(
      VariableDeclarationExpr variableDeclarationExpr,
      MethodCallExpr startExpr,
      List<String> variablesOfInterest,
      DataFlowGraphGenerator dataFlowGraphGenerator) {
    for (VariableDeclarator variableDeclarator : variableDeclarationExpr.getVariables()) {
      // Left side: Variable's name
      String left = variableDeclarator.getNameAsString();
      // Right side: Initializer (may not always be present)
      Optional<Expression> initializer = variableDeclarator.getInitializer();

      if (initializer.isPresent()) {
        Expression right = initializer.get();

        // Check if right side is variable from variablesOfInterest list
        if (right instanceof NameExpr) {
          if (variablesOfInterest.contains(right.toString())) {
            dataFlowGraphGenerator.addVariable(left, left);
            dataFlowGraphGenerator.addEdge(right.toString(), left, "");
            return Optional.of(left);
          }
        }

        // Check if right side is method call expression
        if (right instanceof MethodCallExpr) {
          /*
           * Loop over methodCallExpr in case it is chained method call expression
           * e. g. x = method().method2().method3()
           */
          Node current = right;
          while (current instanceof MethodCallExpr) {
            MethodCallExpr methodCall = (MethodCallExpr) current;

            // Check if method call is startExpr
            if (methodCall
                .asMethodCallExpr()
                .getNameAsString()
                .equals(startExpr.getNameAsString())) {
              dataFlowGraphGenerator.addMethod(
                  startExpr.getNameAsString(), startExpr.getNameAsString() + "()");
              dataFlowGraphGenerator.addVariable(left, left);
              dataFlowGraphGenerator.addEdge(startExpr.getNameAsString(), left, "");
              return Optional.of(left);
            }

            // check if any arguments inside methodCall are variablesOfInterest
            if (ExpressionInspector.checkMethodCallForSpecificArguments(
                methodCall.asMethodCallExpr(), variablesOfInterest)) {
              dataFlowGraphGenerator.addVariable(left, left);
              dataFlowGraphGenerator.addEdge(methodCall.toString(), left, "");
              return Optional.of(left);
            }

            /*
             * Check if method is invoked from another method call expression
             * e.g. x = method().method2() where method2 is the current method call
             * we want to get method() as the scope
             */
            current = methodCall.getScope().orElse(null);
          }

          // Check if method is invoked from a variableOfInterest
          if (current != null) {
            /*
             * Check if root of the chain is variable from variablesOfInterest list
             * e. g. x = y.method().method2() where y is in variablesOfInterest
             */
            if (current instanceof NameExpr) {
              if (variablesOfInterest.contains(current.toString())) {
                dataFlowGraphGenerator.addVariable(left, left);
                dataFlowGraphGenerator.addEdge(current.toString(), left, "");
                return Optional.of(left);
              }
            }
          }
        }
      }
    }

    return Optional.empty();
  }

  public static boolean checkDataFlow(
      MethodCallExpr currentExpr,
      MethodCallExpr targetExpr,
      List<String> variablesOfInterest,
      DataFlowGraphGenerator dataFlowGraphGenerator) {

    if (ExpressionInspector.checkIfTwoMethodsAreEqual(currentExpr, targetExpr)) {
      // check if any arguments are from variablesOfInterest list
      for (Expression argument : currentExpr.getArguments()) {

        /*
         * check if argument is CastExpr
         * e. g. method((String) x)
         */
        if (argument.isCastExpr() && argument.asCastExpr().getExpression().isNameExpr()) {
          if (variablesOfInterest.contains(
              argument.asCastExpr().getExpression().asNameExpr().getNameAsString())) {
            dataFlowGraphGenerator.addMethod(
                currentExpr.getNameAsString(), currentExpr.getNameAsString() + "()");
            dataFlowGraphGenerator.addEdge(
                argument.asCastExpr().getExpression().asNameExpr().getNameAsString(),
                currentExpr.getNameAsString(),
                "");
            return true;
          }
        }

        /*
         * check if argument is NameExpr
         * e. g. method(x)
         */
        if (argument.isNameExpr() && variablesOfInterest.contains(argument.toString())) {
          dataFlowGraphGenerator.addMethod(
              currentExpr.getNameAsString(), currentExpr.getNameAsString() + "");
          dataFlowGraphGenerator.addEdge(argument.toString(), currentExpr.getNameAsString(), "");
          return true;
        }
      }
    }
    return false;
  }
}
