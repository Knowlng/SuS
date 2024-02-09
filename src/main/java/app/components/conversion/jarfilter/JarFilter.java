package app.components.conversion.jarfilter;

import app.utils.CreateOutputDir;
import app.utils.CreatePathWithExtension;
import app.utils.ReadFilesFromDirectory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.utils.IOUtils;

public class JarFilter {

  private static final String CLASS_OUTPUT_DIR = "FilteredJarFiles";
  private static final List<String> EXCLUDED_FOLDERS = Arrays.asList("android/", "androidx/");
  private static Path outputDir;

  /**
   * Filters JAR files found in a specified directory, copying them to a target directory after
   * excluded folders that are not needed.
   *
   * @param jarFolderPath The {@link Path} to the directory containing JAR files to be filtered.
   * @param unzippedPath The {@link Path} to the directory where the filtered JAR files will be
   *     saved.
   * @throws IOException If an I/O error occurs during the filtering process.
   */
  public static void filterLibs(Path jarFolderPath, Path unzippedPath) throws IOException {

    outputDir = CreateOutputDir.createDir(unzippedPath, CLASS_OUTPUT_DIR);

    List<File> jarFileList =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(jarFolderPath, ".jar");

    for (File file : jarFileList) {
      Path newJarFile = CreatePathWithExtension.create(file.toPath(), outputDir, ".jar");
      try {
        if (!Files.exists(newJarFile)) {
          Files.createFile(newJarFile);
        }
      } catch (IOException e) {
        System.err.println(
            "An error occured, could not create jar output file to remove unnecessary folders for file : "
                + file);
        throw e;
      }
      try {
        filter(file, newJarFile.toFile());
      } catch (IOException e) {
        System.err.println("An error occured during unnecessary folder filtering: ");
        e.printStackTrace();
      }
    }
  }

  /**
   * Filters the contents (folders) of JAR file, writing the result to a target JAR file based on
   * predefined criteria.
   *
   * @param sourceJar The source JAR file to be filtered.
   * @param targetJar The target JAR file where the filtered contents are written to.
   * @throws IOException If an error occurs during the read/write process.
   */
  private static void filter(File sourceJar, File targetJar) throws IOException {

    try (ZipFile source = new ZipFile(sourceJar);
        ZipArchiveOutputStream target =
            new ZipArchiveOutputStream(new FileOutputStream(targetJar))) {

      Enumeration<ZipArchiveEntry> entries = source.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        String name = entry.getName();

        if (EXCLUDED_FOLDERS.stream().noneMatch(name::startsWith)) {
          target.putArchiveEntry(new ZipArchiveEntry(name));
          if (!entry.isDirectory()) {
            try (InputStream in = source.getInputStream(entry)) {
              IOUtils.copy(in, target);
            }
          }
          target.closeArchiveEntry();
        }
      }
    }
  }

  public static Path getOutputDir() {
    return outputDir;
  }
}
