package app.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class provides a method to create a new directory from a specified path. It checks if the
 * directory already exists to prevent unnecessary duplication.
 */
public class CreateOutputDir {
  /**
   * Creates a directory from the specified path and directory name. If the directory already
   * exists, this method does not create a new one.
   *
   * @param sourcePath A {@link Path} where the new directory is created.
   * @param dirName The name of the directory to create.
   * @return A Path to the directory.
   * @throws IOException if an I/O error occurs, if the directory creation failed.
   */
  public static Path createDir(Path sourcePath, String dirName) throws IOException {
    Path outputDir = sourcePath.resolve(dirName);
    try {
      if (!Files.exists(outputDir)) {
        Files.createDirectories(outputDir);
      }
    } catch (IOException e) {
      System.err.println(
          "An error occurred during the directory creation process in: " + outputDir);
      throw e;
    }

    return outputDir;
  }
}
