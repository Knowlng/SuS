package app.components.parsing.javaparsing.codeparsing;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class that provides static methods to inspect and retrieve specific pieces of information
 * from expression nodes within the AST (Abstract Syntax Tree).
 */
public class ExpressionInspector {

  /**
   * Retrieves the name of the variable that invoked the expression, if available.
   *
   * @param <T> a subclass of {@link Expression}
   * @param expr the expression to inspect
   * @return the name of the variable that invoked the expression or null if the expression was not
   *     invoked from a variable
   */
  public static <T extends Expression> Optional<String> getVariableThatInvoked(T expr) {
    if (expr instanceof MethodCallExpr) {
      MethodCallExpr methodExpr = (MethodCallExpr) expr;
      Optional<Expression> scope = methodExpr.getScope();
      if (scope.isPresent()) {
        return Optional.of(scope.get().toString());
      }
    }
    return Optional.empty();
  }

  /**
   * Retrieves the name of the variable that the result of the expression is assigned to, if
   * available.
   *
   * @param <T> a subclass of {@link Expression}
   * @param expr the expression to inspect
   * @return the name of the variable that the result of the expression is assigned to or null if
   *     the result is not assigned to a variable
   */
  public static <T extends Expression> Optional<String> getVariableThatResultAssignedTo(T expr) {
    Optional<Node> optParentNode = expr.getParentNode();

    if (optParentNode.isPresent()) {
      Node parentNode = optParentNode.get();

      if (parentNode instanceof AssignExpr) {
        AssignExpr assignExpr = (AssignExpr) parentNode;
        return Optional.of(assignExpr.getTarget().toString());
      } else if (parentNode instanceof VariableDeclarator) {
        VariableDeclarator variableDeclarator = (VariableDeclarator) parentNode;
        return Optional.of(variableDeclarator.getNameAsString());
      }
    }
    return Optional.empty();
  }

  /**
   * Parses the provided {@link NodeList} of {@link Expression} objects representing method
   * arguments, and returns a list of {@link Argument} objects that include information about the
   * type and string representation of each argument.
   *
   * @param Args a {@link NodeList} of method arguments to parse
   * @return a {@link List} of {@link Argument} objects representing the provided arguments or null
   *     if the Args is empty
   */
  public static List<Argument> getArguments(NodeList<Expression> Args) {
    if (Args.isEmpty()) {
      return null;
    }

    List<Argument> Arguments = new ArrayList<>();
    for (Expression argument : Args) {
      String argumentType = getArgumentType(argument);
      String argumentValue = argument.toString();
      Arguments.add(new Argument(argumentType, argumentValue));
    }
    return Arguments;
  }

  public static String getArgumentType(Expression argument) {
    if (argument instanceof StringLiteralExpr) {
      return "String";
    } else if (argument instanceof IntegerLiteralExpr) {
      return "Integer";
    } else if (argument instanceof DoubleLiteralExpr) {
      return "Double";
    } else if (argument instanceof BooleanLiteralExpr) {
      return "Boolean";
    } else if (argument instanceof LiteralExpr) {
      return "Literal";
    } else if (argument instanceof NameExpr) {
      return "Variable";
    } else if (argument instanceof MethodCallExpr) {
      return "Method";
    } else {
      return "Expression";
    }
  }

  /**
   * Checks if the provided {@link MethodCallExpr} contains any arguments from the provided list of
   * variables.
   *
   * @param methodCall the {@link MethodCallExpr} to inspect
   * @param variables the list of variables to check for
   * @return true if the method call contains any arguments that are also contained in the provided
   *     list of variables, false otherwise
   */
  public static boolean checkMethodCallForSpecificArguments(
      MethodCallExpr methodCall, List<String> variables) {
    for (Expression argument : methodCall.getArguments()) {
      if (argument.isNameExpr() && variables.contains(argument.toString())) {
        return true;
      }
    }
    return false;
  }

  public static boolean checkIfTwoMethodsAreEqual(
      MethodCallExpr currentExpr, MethodCallExpr targetExpr) {
    return targetExpr.getNameAsString().equals(currentExpr.getNameAsString());
  }
}
