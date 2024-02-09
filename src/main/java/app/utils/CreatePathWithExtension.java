package app.utils;

import java.nio.file.Path;

public class CreatePathWithExtension {

  /**
   * Creates a String representing a path to the file with a specified extension
   *
   * @param filePath {@link Path} of the file being converted
   * @param outputDir {@link Path} to a directory in which to append the file path
   * @return A String representing a path to the file.
   */
  public static Path create(Path filePath, Path outputDir, String extension) {
    String fileName = filePath.getFileName().toString();

    String name = fileName.replaceFirst("[.][^.]+$", extension);

    return outputDir.resolve(name);
  }
}
