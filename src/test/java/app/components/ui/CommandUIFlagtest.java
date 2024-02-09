package app.components.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandUIFlagTest {
  private String apkPath = "src/test/resources/apk/calc.apk";
  private String jsonPath = "src/main/resources/json/dangerousPatterns.json";
  private CommandUI commandUI;

  @BeforeEach
  public void setUp() {
    ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
    Scanner scanner = new Scanner(in);
    commandUI = new CommandUI(scanner);
  }

  @Test
  public void testAllFlagCommands(@TempDir Path tempDir) throws Exception {
    String[] args = {
      "-mt",
      "true",
      "-outdir",
      tempDir.toString(),
      "-outpdf",
      tempDir.toString(),
      "-json",
      jsonPath,
      apkPath
    };

    Method validateArgumentsMethod =
        CommandUI.class.getDeclaredMethod("validateArguments", String[].class);
    validateArgumentsMethod.setAccessible(true);
    validateArgumentsMethod.invoke(commandUI, (Object) args);

    // Assert apkPath
    Field apkPathField = CommandUI.class.getDeclaredField("apkPath");
    apkPathField.setAccessible(true);
    String apkPathVariable = (String) apkPathField.get(null);

    assertNotNull(apkPathVariable, "APK path should not be null");

    // Assert outputPath
    Field apkDirPathField = CommandUI.class.getDeclaredField("outputPath");
    apkDirPathField.setAccessible(true);
    Path apkDirPath = (Path) apkDirPathField.get(commandUI); // Cast to Path instead of String

    assertNotNull(apkDirPath, "Output directory path should not be null");

    // Assert pdfOutputPath
    Field pdfOutputPathField = CommandUI.class.getDeclaredField("pdfOutputPath");
    pdfOutputPathField.setAccessible(true);
    String pdfOutputPathVariable = (String) pdfOutputPathField.get(commandUI);

    assertNotNull(pdfOutputPathVariable, "PDF output path should not be null");

    // Assert isMultiThreadingOn
    Field isMultiThreadingOnField = CommandUI.class.getDeclaredField("isMultiThreadingOn");
    isMultiThreadingOnField.setAccessible(true);
    Boolean isMultiThreadingOnVariable = (Boolean) isMultiThreadingOnField.get(commandUI);

    assertNotNull(isMultiThreadingOnVariable, "Multi-threading flag should not be null");

    // Assert jsonPath
    Field jsonPatternsPath = CommandUI.class.getDeclaredField("jsonPatternsPath");
    jsonPatternsPath.setAccessible(true);
    String jsonPatternsPathVariable = (String) jsonPatternsPath.get(commandUI);

    assertNotNull(jsonPatternsPathVariable, "JSON patterns path should not be null");
  }

  @Test
  void testInvalidOutpdfFlagPath() throws Exception {
    testExceptionThrownByMethod(
        "validateArguments", new String[] {"-outpdf", "/testing"}, IllegalArgumentException.class);
  }

  @Test
  void testInvalidOutdirFlagPath() throws Exception {
    testExceptionThrownByMethod(
        "validateArguments", new String[] {"-outdir", "/testing"}, IllegalArgumentException.class);
  }

  @Test
  void testInvalidAnalysisFlagPath() throws Exception {
    testExceptionThrownByMethod(
        "validateArguments", new String[] {"-analyze", "/testing"}, IllegalArgumentException.class);
  }

  void testInvalidJsonFlagPath() throws Exception {
    testExceptionThrownByMethod(
        "validateArguments", new String[] {"-json", "/testing"}, IllegalArgumentException.class);
  }

  private void testExceptionThrownByMethod(
      String methodName, Object args, Class<? extends Throwable> expectedException)
      throws Exception {
    Method method = CommandUI.class.getDeclaredMethod(methodName, String[].class);
    method.setAccessible(true);

    Throwable exception =
        assertThrows(InvocationTargetException.class, () -> method.invoke(commandUI, args))
            .getCause();

    assertTrue(expectedException.isInstance(exception));
  }

  @Test
  private void testHelpFlagExitsSafely() throws Exception {
    String[] args = {"-help"};

    Method validateArgumentsMethod =
        CommandUI.class.getDeclaredMethod("validateArguments", String[].class);
    validateArgumentsMethod.setAccessible(true);

    // Assert that an IllegalArgumentException is thrown
    assertThrows(
        IllegalArgumentException.class,
        () -> validateArgumentsMethod.invoke(commandUI, (Object) args),
        "IllegalArgumentException should be thrown when -help flag is used.");
  }
}
