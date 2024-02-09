package app.components.conversion.unzip;

import app.utils.ReadFilesFromDirectory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

/** Represents a utility class that can unzip APK files. */
public class UnzipFile {

  /**
   * Takes an APK file's path, unzips (extracts) its content, and saves it in a specified directory.
   * If an XAPK file is provided unzipps all APK inside of the XAPK unzipped directory
   *
   * @param apkFilePath Full system path to the APK file to be unzipped.
   * @param outputPath A {@link String} where a directory with unzipped files will be created.
   * @return A {@link Path} where the APK content has been extracted.
   * @throws IOException If there's an issue accessing or manipulating the file.
   */
  public static Path unzip(String apkFilePath, String outputPath) throws IOException {
    File apkFile = new File(apkFilePath);

    if (!apkFile.exists()) {
      throw new FileNotFoundException("File does not exist: " + apkFilePath);
    }
    String apkFileName = apkFile.getName();
    String baseName = apkFileName.substring(0, apkFileName.lastIndexOf('.'));

    File destDir = new File(outputPath, baseName);

    if (!destDir.exists() && !destDir.mkdirs()) {
      throw new IOException("Could not create destination directory: " + destDir.getAbsolutePath());
    }

    // Open the APK file as a ZIP file to read its content.
    try (ZipFile zipFile = new ZipFile(apkFile)) {
      // Go through all the items inside the ZIP file.
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();

        // If the item is not a directory, extract it.
        if (!entry.isDirectory()) {
          File entryDestination = new File(destDir, entry.getName());
          // Create directory for this file if it doesn't exist.
          File parent = entryDestination.getParentFile();
          if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Couldn't create directory: " + parent.getAbsolutePath());
          }

          // Actually extract the file's content.
          try (InputStream in = zipFile.getInputStream(entry);
              OutputStream out = Files.newOutputStream(entryDestination.toPath())) {
            IOUtils.copy(in, out);
          } catch (IOException e) {
            System.err.println(
                "An error ocurred while unzipping file: "
                    + entry.getName()
                    + " - "
                    + e.getMessage()
                    + ", skipping...");
          }
        }
      }
    } catch (IOException e) {
      System.err.println("An error occured during unzipping:");
      throw e;
    }
    // if xapk is provided unzips all APK files inside to the same temp folder
    if (apkFilePath.toLowerCase().endsWith(".xapk")) {
      List<File> apkList =
          ReadFilesFromDirectory.getSpecificFilesFromDirectory(destDir.toPath(), ".apk");
      for (File apk : apkList) {
        unzip(apk.toString(), destDir.toString());
      }
    }
    // Return the path of the directory where we extracted everything.
    return destDir.toPath();
  }
}
