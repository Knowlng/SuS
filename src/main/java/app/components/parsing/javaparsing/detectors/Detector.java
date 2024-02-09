package app.components.parsing.javaparsing.detectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;

public interface Detector {
  void detect(CompilationUnit AST);

  Expression getDetectedExpression();

  void reset();
}
