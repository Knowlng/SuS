package app.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadFilesFromDirectory {
  /**
   * Retrieves all specific files that match the provided criteria in a specified directory and its
   * subdirectories
   *
   * <p>- If the search criteria starts with a dot ('.'), it is considered an extension, otherwise,
   * the search criteria is considered a filename
   *
   * @param directoryPath A {@link Path} representing the root directory path
   * @param searchCriteria A {@link String} representing the search criteria to look for
   * @return A {@link List} of {@link File} objects representing files with specified criteria
   * @throws IOException if an error ocurred during file retrieval
   * @throws NoSuchFileException if no files were found with the given criteria
   */
  public static List<File> getSpecificFilesFromDirectory(Path directoryPath, String searchCriteria)
      throws IOException {
    try (Stream<Path> paths = Files.walk(directoryPath)) {
      List<File> files = findSpecificFiles(paths, searchCriteria);
      if (files.isEmpty()) {
        throw new NoSuchFileException(
            "No files with extension " + searchCriteria + " found in " + directoryPath);
      }
      return files;
    } catch (IOException e) {
      System.err.println(
          "An error occurred during " + searchCriteria + " file retrieval from " + directoryPath);
      throw e;
    }
  }

  /**
   * Filters and converts the provided Stream of paths into Java file references.
   *
   * @param paths A {@link Stream} of {@link Path} objects.
   * @return A {@link List} of {@link File} objects filtered to include only files with specified
   *     extension.
   */
  private static List<File> findSpecificFiles(Stream<Path> paths, String searchCriteria) {
    return paths
        .filter(Files::isRegularFile)
        .filter(path -> matchesCriteria(path, searchCriteria))
        .map(Path::toFile)
        .collect(Collectors.toList());
  }

  /**
   * Determines if the given path matches the specified search criteria.
   *
   * @param path The {@link Path} of the file to be checked
   * @param searchCriteria A {@link String} representing the search criteria
   * @return if searchCriteria starts with a dot, looks for file with specified extensions,
   *     otherwise matches full file names
   */
  private static boolean matchesCriteria(Path path, String searchCriteria) {
    if (searchCriteria.startsWith(".")) {
      return path.toString().endsWith(searchCriteria);
    } else {
      return path.getFileName().toString().equals(searchCriteria);
    }
  }
}
