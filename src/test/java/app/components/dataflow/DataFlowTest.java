package app.components.dataflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.components.parsing.javaparsing.codeparsing.PatternDetector;
import app.components.parsing.javaparsing.codeparsing.dataflow.DataFlow;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataFlowTest {

  private DataFlow dataFlow;
  private PatternDetector patternDetector;

  @BeforeEach
  public void setUp() {
    patternDetector = new PatternDetector("test", 1, true, "Description of test pattern", "High");
    dataFlow = new DataFlow();
  }

  @Test
  public void testDirectDataFlow() {
    // Arrange
    String sourceCode =
        "class A {"
            + "void main() {"
            + "   Intent intent = getIntent();"
            + "   webview.loadUrl(intent);"
            + "}"
            + "}";

    CompilationUnit AST = StaticJavaParser.parse(sourceCode);

    MethodCallExpr startExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("getIntent"))
            .orElse(null);
    MethodCallExpr endExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("loadUrl"))
            .orElse(null);

    boolean result = dataFlow.checkDataFlowBetweenMethods(AST, startExpr, endExpr, patternDetector);

    assertTrue(result);
  }

  @Test
  public void testInDirectDataFlow() {
    // Arrange
    String sourceCode =
        "class A {"
            + "void main() {"
            + "   Intent intent = getIntent();"
            + "   Uri data = intent.getData();"
            + "   String values = data.getQueryParameter(\"url\");"
            + "   webview.loadUrl(values);"
            + "}"
            + "}";

    CompilationUnit AST = StaticJavaParser.parse(sourceCode);

    MethodCallExpr startExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("getIntent"))
            .orElse(null);
    MethodCallExpr endExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("loadUrl"))
            .orElse(null);

    boolean result = dataFlow.checkDataFlowBetweenMethods(AST, startExpr, endExpr, patternDetector);

    assertTrue(result);
  }

  @Test
  public void testChainedDataFlow() {
    // Arrange
    String sourceCode =
        "class A {"
            + "void main() {"
            + "   String values = getIntent().getData().getQueryParameter(\"url\");"
            + "   webview.loadUrl(values);"
            + "}"
            + "}";

    CompilationUnit AST = StaticJavaParser.parse(sourceCode);

    MethodCallExpr startExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("getIntent"))
            .orElse(null);
    MethodCallExpr endExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("loadUrl"))
            .orElse(null);

    boolean result = dataFlow.checkDataFlowBetweenMethods(AST, startExpr, endExpr, patternDetector);

    assertTrue(result);
  }

  @Test
  public void testNoDataFlow() {
    // Arrange
    String sourceCode =
        "class A {"
            + "void main() {"
            + "   Intent intent = getIntent();"
            + "   Uri data = intent.getData();"
            + "   String variable = \"noDataFlow\";"
            + "   String values = data.getQueryParameter(\"url\");"
            + "   webview.loadUrl(variable);"
            + "}"
            + "}";

    CompilationUnit AST = StaticJavaParser.parse(sourceCode);

    MethodCallExpr startExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("getIntent"))
            .orElse(null);
    MethodCallExpr endExpr =
        AST.findFirst(MethodCallExpr.class, m -> m.getNameAsString().equals("loadUrl"))
            .orElse(null);

    boolean result = dataFlow.checkDataFlowBetweenMethods(AST, startExpr, endExpr, patternDetector);

    assertFalse(result);
  }
}
