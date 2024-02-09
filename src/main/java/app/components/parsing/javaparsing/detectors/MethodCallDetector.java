package app.components.parsing.javaparsing.detectors;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Optional;

/**
 * A specialized detector class for method calls without specific arguments within an Abstract
 * Syntax Tree (AST).
 */
public class MethodCallDetector extends VoidVisitorAdapter<Void> implements Detector {
  private PatternDetector patternDetector;
  private final String methodToDetect;
  private boolean isMethodDetected;
  private MethodCallExpr detectedMethodCall;

  public MethodCallDetector(String methodToDetect, PatternDetector patternDetector) {
    this.patternDetector = patternDetector;
    this.methodToDetect = methodToDetect;
    this.isMethodDetected = false;
  }

  @Override
  public Expression getDetectedExpression() {
    return detectedMethodCall;
  }

  @Override
  public void reset() {
    isMethodDetected = false;
  }

  /**
   * Initiates the detection process on the provided AST. It starts the visitation process which
   * continues until the entire tree has been traversed and all relevant method call expressions
   * have been detected.
   *
   * @param AST The CompilationUnit representing the Abstact syntax tree of the source code to be
   *     analyzed.
   */
  @Override
  public void detect(CompilationUnit AST) {
    visit(AST, null);
  }

  /**
   * Visits and analyzes method calls within the AST. If the expression matches the method to
   * detect, it captures its details in a DetectionResults object and interacts with the associated
   * PatternDetector.
   *
   * @param expr The MethodCallExpr currently being visited.
   * @param arg An unused Void parameter, present due to Visitor pattern requirements.
   */
  @Override
  public void visit(MethodCallExpr expr, Void arg) {
    // If target method call is detected return early
    if (isMethodDetected) {
      return;
    }
    if (isMethodDetected(expr)) {
      handleDetectedMethod(expr);
      return;
    }
    super.visit(expr, arg);
  }

  private boolean isMethodDetected(MethodCallExpr expr) {
    return expr.getNameAsString().equals(methodToDetect);
  }

  protected void handleDetectedMethod(MethodCallExpr expr) {
    patternDetector.incrementState();

    // get the parent method declaration in which expr exists
    @SuppressWarnings("unchecked")
    Optional<MethodDeclaration> parentMethod = expr.findAncestor(MethodDeclaration.class);
    if (parentMethod.isPresent()) {
      patternDetector.addMethodDeclaration(parentMethod.get());
    }

    isMethodDetected = true;
    detectedMethodCall = expr;

    if (patternDetector.isRequiredState()) {
      patternDetector.execute();
    }
  }

  @Override
  public String toString() {

    return "MethodCallDetector{"
        + "typeToDetect='"
        + methodToDetect
        + '\''
        + "patternDetector='"
        + patternDetector.getName()
        + '}';
  }
}
