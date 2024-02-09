package app.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutputSilencer {
  private static final PrintStream originalOut = System.out;

  /** Silences the System.out output. */
  public static void silenceOutput() {
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
  }

  /** Restores the original System.out output. */
  public static void restoreOutput() {
    System.setOut(originalOut);
  }
}
