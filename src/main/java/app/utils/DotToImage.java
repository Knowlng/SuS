package app.utils;

import app.components.conversion.ConversionProcess;
import app.components.ui.CommandUI;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DotToImage {
  private static final String PATH_TO_GRAPH_FOLDER =
      File.separator + "dataflow_graph" + File.separator;
  private static String outputFilePath;

  public static String convert(String dotString, String specifiedDir) {
    outputFilePath = specifiedDir;
    return startConversion(dotString);
  }

  public static String convert(String dotString) {
    String apkFilePath = CommandUI.getApkPath();
    if (apkFilePath == null) {
      outputFilePath = CommandUI.analysisDirPath().toString() + PATH_TO_GRAPH_FOLDER;
    } else {
      outputFilePath = ConversionProcess.getOutputPath() + PATH_TO_GRAPH_FOLDER;
    }
    return startConversion(dotString);
  }

  private static String startConversion(String dotString) {
    try {
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      outputFilePath = outputFilePath + timeStamp + ".png";

      if (isGraphvizInstalled()) {
        Graphviz.fromString(dotString).render(Format.PNG).toFile(new File(outputFilePath));
      } else {
        System.err.println("Graphviz is not installed. Unable to render graph.");
        return null;
      }
      return outputFilePath;
    } catch (Exception e) {
      System.err.println("Error generating Graphviz diagram: " + e.getMessage());
      return null;
    }
  }

  private static boolean isGraphvizInstalled() {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("dot", "-V");
      Process process = processBuilder.start();
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }
}
