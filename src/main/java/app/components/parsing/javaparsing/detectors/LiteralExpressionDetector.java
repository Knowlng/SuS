package app.components.parsing.javaparsing.detectors;

import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;

public class LiteralExpressionDetector extends VoidVisitorAdapter<Void> {
  private List<String> strings = new ArrayList<>();

  @Override
  public void visit(StringLiteralExpr n, Void arg) {
    super.visit(n, arg);
    strings.add(n.getValue());
  }

  public List<String> getStrings() {
    return strings;
  }
}
