package app.components.parsing.javaparsing.detectors;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Optional;

/** A specialized detector class for object creation calls within an Abstract Syntax Tree (AST). */
public class ObjectCreationDetector extends VoidVisitorAdapter<Void> implements Detector {
  private PatternDetector patternDetector;
  private final String typeToDetect;
  private boolean isObjectCreationDetected;
  private ObjectCreationExpr detectedObjectCreation;

  public ObjectCreationDetector(String typeToDetect, PatternDetector patternDetector) {
    this.patternDetector = patternDetector;
    this.typeToDetect = typeToDetect;
    this.isObjectCreationDetected = false;
  }

  @Override
  public Expression getDetectedExpression() {
    return detectedObjectCreation;
  }

  @Override
  public void reset() {
    isObjectCreationDetected = false;
  }

  /**
   * Initiates the detection process on the provided AST. It starts the visitation process which
   * continues until the entire tree has been traversed and all relevant object creation expressions
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
   * Visits and analyzes object creation expressions within the AST. If the expression matches the
   * type to detect, it captures its details in a DetectionResults object and interacts with the
   * associated PatternDetector.
   *
   * @param expr The ObjectCreationExpr currently being visited.
   * @param arg An unused Void parameter, present due to Visitor pattern requirements.
   */
  @Override
  public void visit(ObjectCreationExpr expr, Void arg) {
    // If target object creation is detected return early
    if (isObjectCreationDetected) {
      return;
    }

    if (isObjectDetected(expr)) {
      patternDetector.incrementState();

      // get the parent method declaration in which expr exists
      @SuppressWarnings("unchecked")
      Optional<MethodDeclaration> parentMethod = expr.findAncestor(MethodDeclaration.class);
      if (parentMethod.isPresent()) {
        patternDetector.addMethodDeclaration(parentMethod.get());
      }

      isObjectCreationDetected = true;
      detectedObjectCreation = expr;

      if (patternDetector.isRequiredState()) {
        patternDetector.execute();
      }
      return; // early exit because the target object creation was found
    }
    super.visit(expr, arg);
  }

  private boolean isObjectDetected(ObjectCreationExpr expr) {
    return expr.getType().asString().equals(typeToDetect);
  }

  @Override
  public String toString() {

    return "ObjectCreationDetector{"
        + "typeToDetect='"
        + typeToDetect
        + '\''
        + "patternDetector='"
        + patternDetector.getName()
        + '\''
        + '}';
  }
}
