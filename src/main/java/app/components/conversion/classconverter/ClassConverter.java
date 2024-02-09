package app.components.conversion.classconverter;

import app.utils.CreateOutputDir;
import app.utils.ReadFilesFromDirectory;
import app.utils.ThreadPoolManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.benf.cfr.reader.Main;

/**
 * Provides a way to convert compiled .jar files into their java source code using CFR decompiler
 */
public class ClassConverter {

  private static final String SOURCE_OUTPUT_FOLDER = "JavaSource";
  private static Path sourceOutputDir;

  /**
   * Converts all JAR files in the specified directory to Java source code, placing the results in a
   * specified output directory. Uses multi-threading if {@param isMultiThreadingOn} .
   *
   * @param jarFolderPath A {@link Path} of the directory containing the JAR files to be converted.
   * @param outputPath A {@link Path} where output folder will be created to store converted source
   *     code files.
   * @param isMultiThreadingOn A {@link Boolean} object that specifies if multi-threading should be
   *     used.
   * @throws IOException if an I/O error occurs reading from the file system or processing JAR
   *     files.
   */
  public static void convert(Path jarFolderPath, Path outputPath, Boolean isMultiThreadingOn)
      throws IOException, InterruptedException {

    sourceOutputDir = CreateOutputDir.createDir(outputPath, SOURCE_OUTPUT_FOLDER);

    List<File> jarFileList =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(jarFolderPath, ".jar");

    if (isMultiThreadingOn) {
      ThreadPoolManager.getInstance().initializeFixedThreadPool(jarFileList.size());
      ThreadPoolManager.getInstance()
          .executeConversionTasks(jarFileList, file -> executeConversion(file));

    } else {
      jarFileList.forEach(file -> executeConversion(file));
    }
  }

  /**
   * Handles the decompilation process for a single class file using CFR decompiler & notifies the
   * user upon successfull file conversion.
   *
   * @param file The class file to be decompiled to Java source code.
   */
  private static void executeConversion(File file) {
    String[] paths = {
      file.toString(), "--outputdir", sourceOutputDir.toString(), "--silent", "true"
    };
    Main.main(paths);
    System.out.println("Completed conversion for file: " + file.getName());
  }

  /**
   * Retrieves the name of the output folder where the decompiled Java source code is stored.
   *
   * @return A string representing the name of the output folder.
   */
  public static String getOutputFolderName() {
    return SOURCE_OUTPUT_FOLDER;
  }
}
