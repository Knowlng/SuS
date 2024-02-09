package app.components.unzip;

import static org.junit.jupiter.api.Assertions.*;

import app.components.conversion.unzip.UnzipFile;
import app.utils.ReadFilesFromDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UnzipFileTest {

  @Test
  void testUnzipSuccessfully(@TempDir Path tempDir) throws IOException {
    Path zipPath = Paths.get("src/test/resources/apk/calc.apk");
    unzip(tempDir, zipPath);
  }

  @Test
  void testUnzipXAPK(@TempDir Path tempDir) throws IOException {
    Path zipPath = Paths.get("src/test/resources/xapk/test.xapk");
    unzip(tempDir, zipPath);
  }

  @Test
  void testUnzipInvalidFile(@TempDir Path tempDir) throws IOException {
    Path invalidZipPath = tempDir.resolve("invalid.apk");
    Files.createFile(invalidZipPath);

    assertThrows(
        IOException.class,
        () -> {
          UnzipFile.unzip(invalidZipPath.toString(), tempDir.toString());
        });
  }

  void unzip(@TempDir Path tempDir, Path zipPath) throws IOException {
    Path extractedPath = UnzipFile.unzip(zipPath.toString(), tempDir.toString());

    File extractedDir = new File(extractedPath.toString());
    assertTrue(extractedDir.exists());
    assertTrue(extractedDir.isDirectory());

    List<File> dexFiles =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(extractedPath, ".dex");
    assertFalse(dexFiles.isEmpty(), "No DEX files were created in the expected folder");

    List<File> xmlFiles =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(extractedPath, ".xml");
    assertFalse(xmlFiles.isEmpty(), "No XML files were created in the expected folder");
  }
}
