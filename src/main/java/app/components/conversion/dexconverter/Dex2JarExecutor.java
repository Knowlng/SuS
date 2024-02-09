package app.components.conversion.dexconverter;

import app.utils.CreateOutputDir;
import app.utils.CreatePathWithExtension;
import app.utils.ReadFilesFromDirectory;
import app.utils.ThreadPoolManager;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/** This class utilizes Dex2Jar tool to perform the conversion from DEX files to JAR files */
public class Dex2JarExecutor {
  private static final String JAR_OUTPUT_DIR = "JarFiles";
  private static String pathToExecutable;
  private static Path outputDir;

  /**
   * Converts DEX files located in a specified directory to JAR files. Uses multi-threading if
   * {@param isMultiThreadingOn} .
   *
   * @param unzippedPath The root directory {@link Path} that contains DEX files to be converted.
   * @param isMultiThreadingOn A {@link Boolean} object that specifies if multi-threading should be
   *     used.
   * @return A {@link Path} pointing to the directory containing the converted JAR files.
   * @throws IOException if an I/O error occurs during the process.
   */
  public static void convertToJar(Path unzippedPath, Boolean isMultiThreadingOn)
      throws IOException, InterruptedException {

    pathToExecutable = setPathToExecutable();

    outputDir = CreateOutputDir.createDir(unzippedPath, JAR_OUTPUT_DIR);

    List<Path> dexFiles = getDexFileList(unzippedPath);

    if (isMultiThreadingOn) {
      ThreadPoolManager.getInstance().initializeFixedThreadPool(dexFiles.size());
      ThreadPoolManager.getInstance()
          .executeConversionTasks(dexFiles, path -> startConversion(path, isMultiThreadingOn));
    } else {
      dexFiles.forEach(path -> startConversion(path, isMultiThreadingOn));
    }
  }

  /**
   * Starts the conversion of a single DEX file to a JAR file. It handles the conversion by calling
   * {@link #convertFile(Path, boolean)} and manages any errors that occur.
   *
   * @param path The {@link Path} of the DEX file to be converted.
   * @param isMultiThreadingOn A {@link Boolean} object that indicates if multi-threading is
   *     enabled.
   */
  private static void startConversion(Path path, Boolean isMultiThreadingOn) {
    try {
      convertFile(path, isMultiThreadingOn);
    } catch (IOException | InterruptedException e) {
      System.err.println("An error occured converting: " + path.toString());
    }
  }

  /**
   * Converts a single DEX file to a JAR file. This method prepares the output path and error file
   * path for the conversion process, creates the necessary files, and then calls {@link
   * #executeConversion(Path, Path, Path, Boolean)} to perform the conversion.
   *
   * @param dexPath The {@link Path} of the DEX file to be converted.
   * @param isMultiThreadingOn A {@link Boolean} object that indicates if multi-threading is
   *     enabled.
   * @throws IOException If there is an error creating the output files.
   * @throws InterruptedException If the conversion process is interrupted.
   */
  private static void convertFile(Path dexPath, Boolean isMultiThreadingOn)
      throws IOException, InterruptedException {

    Path outputPath = CreatePathWithExtension.create(dexPath, outputDir, ".jar");
    Path errorFilePath = CreatePathWithExtension.create(dexPath, outputDir, ".zip");

    try {

      if (!Files.exists(outputPath)) {
        Files.createFile(outputPath);
      }

    } catch (IOException e) {
      System.err.println(
          "An error occured, could not create jar output file for dex conversion for file: "
              + dexPath);
      if (isMultiThreadingOn) {
        return;
      } else {
        throw e;
      }
    }
    executeConversion(outputPath, errorFilePath, dexPath, isMultiThreadingOn);
  }

  /**
   * Executes the conversion of a DEX file to a JAR file. This method uses {@link ProcessBuilder} to
   * start the dex2jar tool process. Notifies the user upon successfull or unsuccessfull file
   * conversion.
   *
   * @param outputPath The {@link Path} where the output JAR file will be saved.
   * @param errorFilePath The {@link Path} where any error file will be saved.
   * @param dexPath The {@link Path} of the DEX file to be converted.
   * @param isMultiThreadingOn A {@link Boolean} object that indicates if is enabled.
   * @throws InterruptedException If the conversion process is interrupted.
   */
  private static void executeConversion(
      Path outputPath, Path errorFilePath, Path dexPath, boolean isMultiThreadingOn)
      throws InterruptedException {
    try {
      ProcessBuilder processBuilder =
          new ProcessBuilder(
              pathToExecutable,
              "-o",
              outputPath.toString(),
              "-e",
              errorFilePath.toString(),
              "--force",
              dexPath.toString());

      Process process = processBuilder.start();

      int exitCode = process.waitFor();

      if (exitCode != 0) {
        System.err.println("An error occurred during the conversion of " + dexPath.getFileName());
      } else {
        System.out.println("Completed conversion for file: " + dexPath.getFileName());
      }
    } catch (IOException e) {
      System.err.println("An IOException occurred during the conversion process:");
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.err.println(
          "The conversion task was interrupted and conversion may be incomplete for: " + dexPath);
      e.printStackTrace();
      if (isMultiThreadingOn) {
        return;
      } else {
        throw e;
      }
    }
  }

  /**
   * Searches for all files with a ".dex" extension within the given unzipped directory path. It
   * utilizes the utility method {@link ReadFilesFromDirectory#getSpecificFilesFromDirectory(Path,
   * String)} for this purpose. Then converts the {@link List} of {@link File} objects to a {@link
   * List} of {@link Path} objects
   *
   * @param unzippedDirPath the {@link Path} of the directory where the method looks for DEX files.
   * @return a list of {@link Path} objects, where each Path points to a DEX file found in the
   *     specified directory.
   * @throws IOException if an I/O error occurs during reading file retrieval or if there are no DEX
   *     files in the specified directory
   */
  private static List<Path> getDexFileList(Path unzippedDirPath) throws IOException {
    List<File> dexFileList =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(unzippedDirPath, ".dex");

    return dexFileList.stream().map(File::toPath).collect(Collectors.toList());
  }

  /**
   * Determines the path to the executable script responsible for the conversion based on the
   * operating system. Also sets the script file to executable on all operating systems
   *
   * @return A String representing a path to the executable script.
   */
  private static String setPathToExecutable() throws IOException {
    String osName = System.getProperty("os.name").toLowerCase();
    String scriptExtension = osName.contains("win") ? ".bat" : ".sh";

    File executableFile =
        new File(
            "tools"
                + File.separator
                + "dex-tools-2.x"
                + File.separator
                + "d2j-dex2jar"
                + scriptExtension);

    pathToExecutable = executableFile.getAbsolutePath();

    setExecutablePermission(executableFile);

    return pathToExecutable;
  }

  /**
   * Sets the script file to executable.
   *
   * @param executableFile file which needs to be set to executable
   * @throws IOException if it is unable to set permissions for the file
   */
  private static void setExecutablePermission(File executableFile) throws IOException {

    if (!executableFile.setExecutable(true)) {
      throw new IOException("Failed to set execute permission for dex2jar tool");
    }
  }

  public static Path getOutputDir() {
    return outputDir;
  }
}
