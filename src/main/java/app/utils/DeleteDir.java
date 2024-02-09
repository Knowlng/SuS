package app.utils;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

/**
 * Provides functionality to recursively delete a directory and all of its sub-directories and
 * files.
 */
public class DeleteDir {
  /**
   * Deletes a specified directory and all of its contents.
   *
   * @param dir The directory to be deleted.
   * @throws IOException if an I/O error occurs when deleting the directory or any of its contents.
   */
  public static void deleteDirectory(Path dir) throws IOException {
    if (Files.isDirectory(dir)) {

      Files.walkFileTree(
          dir,
          EnumSet.noneOf(FileVisitOption.class),
          Integer.MAX_VALUE,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              Files.delete(file);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            }
          });
    }
  }
}
