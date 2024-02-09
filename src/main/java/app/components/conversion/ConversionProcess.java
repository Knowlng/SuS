package app.components.conversion;

import app.components.conversion.classconverter.ClassConverter;
import app.components.conversion.dexconverter.Dex2JarExecutor;
import app.components.conversion.jarfilter.JarFilter;
import app.components.conversion.unzip.UnzipFile;
import app.components.conversion.xmlconverter.XMLConverter;
import app.utils.ReadFilesFromDirectory;
import app.utils.ThreadPoolManager;
import app.utils.Timer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for unzipping user provided files, converting DEX files to JAR,
 * filtering JAR files, and converting class files to source code. This class also handles binary
 * XML conversion
 */
public class ConversionProcess {
  private static Path outputPath;
  private static Timer timer = new Timer();
  private static List<String> convertedXMLStringList = new ArrayList<>();

  /**
   * Initiates the conversion process for an APK file. This process involves these steps: 1.
   * Unzipping the APK file to a temporary or a user specified directory. 2. onverting XML files
   * found within the unzipped APK. 3. Converting DEX files to JAR format. 4. Filtering unnecessary
   * JAR files. 5. Converting class files in the JARs to source code.
   *
   * <p>Throughout the process, progress messages are printed, and time taken for key steps is
   * logged. If multithreading is enabled, parallel processing is used for certain tasks to improve
   * performance.
   *
   * @param apkPath The file path of the APK to be converted.
   * @param isMultiThreadingOn Flag indicating whether multithreading should be used for conversion.
   * @param outputDirPath Flag indicating where the conversion process should store its output.
   * @throws IOException If an I/O error occurs during the conversion proce s.
   * @throws InterruptedException If the thread executing the conversion is interrupted.
   */
  public static void startConversion(String apkPath, Boolean isMultiThreadingOn, Path outputDirPath)
      throws IOException, InterruptedException {
    timer.start();

    if (outputDirPath != null) {
      outputPath = UnzipFile.unzip(apkPath, outputDirPath.toString());
    } else {
      outputPath = UnzipFile.unzip(apkPath, System.getProperty("java.io.tmpdir"));
    }
    System.out.println("APK unzipped in: " + outputPath);

    System.out.println("\nConverting XML files, please wait...");
    convertXML();

    System.out.println("\nDEX files are being converted to JAR files, please wait...");
    Dex2JarExecutor.convertToJar(outputPath, isMultiThreadingOn);
    Timer.printDuration("DEX to JAR conversion", timer.lap());

    System.out.println("\nJAR files have been created, checking for unnecessary folders...");
    JarFilter.filterLibs(Dex2JarExecutor.getOutputDir(), outputPath);

    System.out.println("\nClass files are being converted to source code, please wait...");
    ClassConverter.convert(JarFilter.getOutputDir(), outputPath, isMultiThreadingOn);
    Timer.printDuration("Class file conversion", timer.lap());
    if (isMultiThreadingOn) {
      ThreadPoolManager.getInstance().shutdownExecutor();
    }
    System.out.println("================================================");
    Timer.printDuration("Total conversion process", timer.stop());
  }

  /**
   * This method looks for all "AndroidManifest.xml" files in the given directory or its
   * subdirectories and converts them to a string format. Then it renames the binary
   * 'AndroidManifest.xml' files to 'AndroidManifest-bin.xml' using {@link #renameBinXMLFiles(File)}
   * and writes the converted xml string to newly created 'AndroidManifest.xml' file. If an error
   * occurs during one of the files retrieval or XML conversion it does not stop the program, but
   * continues to look for and convert other present files
   */
  private static void convertXML() {
    try {
      List<File> xmlFileList =
          ReadFilesFromDirectory.getSpecificFilesFromDirectory(outputPath, "AndroidManifest.xml");
      for (File xmlFile : xmlFileList) {
        String xmlContent = XMLConverter.convertToXmlString(xmlFile.getPath());
        if (xmlContent != null && !xmlContent.isEmpty()) {
          convertedXMLStringList.add(xmlContent);

          Path convertedXmlFilePath = Paths.get(xmlFile.getParent(), "AndroidManifest.xml");

          renameBinXMLFiles(xmlFile);

          Files.write(convertedXmlFilePath, xmlContent.getBytes());
        } else {
          System.err.println(
              "A conversion error occurred or XML file " + xmlFile + " is empty, skipping...");
        }
      }
    } catch (NoSuchFileException e) {
      System.err.println("No AndroidManifest.xml files found in " + outputPath);
    } catch (IOException e) {
      System.err.println(
          "An error occurred during AndroidManifest.xml file retrieval from " + outputPath);
    }
  }

  /**
   * @return The output path where the APK contents were extracted.
   */
  public static Path getOutputPath() {
    return outputPath;
  }

  /**
   * Renames a specified XML file to "AndroidManifest-bin.xml" in its current directory.
   *
   * @param xmlFile The XML file to be renamed.
   */
  private static void renameBinXMLFiles(File xmlFile) {
    String parentDir = xmlFile.getParent();
    File newFile = new File(parentDir, "AndroidManifest-bin.xml");
    xmlFile.renameTo(newFile);
  }

  /**
   * @return A list of converted XML strings.
   */
  public static Optional<List<String>> getConvertedXMLStringList() {
    return convertedXMLStringList.isEmpty()
        ? Optional.empty()
        : Optional.of(convertedXMLStringList);
  }
}
