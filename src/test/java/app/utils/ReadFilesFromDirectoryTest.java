package app.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReadFilesFromDirectoryTest {

  @Test
  void testGetSpecificFilesFromDirectoryWithJavaFiles() throws IOException {
    Path testDirPath = Paths.get("src/test/resources/java");

    List<File> javaFiles =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(testDirPath, ".java");

    assertFalse(javaFiles.isEmpty(), "No Java files found in the directory");
  }

  @Test
  void testGetSpecificFilesFromDirectoryWithSpecifiedFileName() throws IOException {
    Path testDirPath = Paths.get("src/test/resources/json");

    List<File> jsonFile =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(testDirPath, "PermissionExample.json");

    assertFalse(jsonFile.isEmpty(), "No Java files found in the directory");
  }

  @Test
  void testGetSpecificFilesFromDirectoryWithNoJavaFiles() {
    Path testDirPath = Paths.get("src/test/resources/json");

    assertThrows(
        NoSuchFileException.class,
        () -> ReadFilesFromDirectory.getSpecificFilesFromDirectory(testDirPath, ".java"),
        "NoSuchFileException was expected when no Java files are present");
  }

  @Test
  void testGetSpecificFilesFromDirectoryWithNonexistentDirectory() {
    Path testDirPath = Paths.get("src/test/resources/nonexistentDir");

    assertThrows(
        IOException.class,
        () -> ReadFilesFromDirectory.getSpecificFilesFromDirectory(testDirPath, ".java"),
        "IOException was expected for a nonexistent directory");
  }
}
