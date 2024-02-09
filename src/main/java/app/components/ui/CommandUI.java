/**
 * The CommandUI class is responsible for managing user input and performing actions related to
 * Android APK analysis. It provides methods for validating the input APK file and extracting its
 * contents.
 */
package app.components.ui;

import app.components.conversion.ConversionProcess;
import app.components.parsing.ParsingProcess;
import app.components.resultsshowcase.PDFConverter;
import app.components.resultsshowcase.Results;
import app.database.databaseFetch.DatabaseFetchAnalysis;
import app.database.databaseInsert.InsertAnalysisResults;
import app.utils.DeleteDir;
import app.utils.FileHashing;
import app.utils.OutputSilencer;
import app.utils.ThreadPoolManager;
import app.utils.Timer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class CommandUI {

  private static String apkPath = null;
  private Boolean isMultiThreadingOn = null;
  private boolean isAnalysisSpecified = false;
  private Scanner scanner;
  private static Path outputPath = null;
  private String pdfOutputPath = null;
  private static Path analysisDirPath = null;
  private boolean verbose = false;
  private static String jsonPatternsPath = null;
  private static String fileHash;
  private static Boolean forceAnalysis = false;
  private static Boolean hashExist = false;
  private static boolean isFetchDatabaseData = false;
  private Timer timer = new Timer();

  /**
   * Constructs a CommandUI object with a given Scanner for user input.
   *
   * @param scanner The Scanner object to use for user input.
   */
  public CommandUI(Scanner scanner) {
    this.scanner = scanner;
  }

  /**
   * Manages user input for the APK analysis. It validates the input APK file path and checks for
   * supported file types (APK, ZIP, XAPK).
   *
   * <p>If user provides a correct path to the APK file, it asks the user if multi-threading should
   * be used, then the method calls {@code ConversionProcess}, {@code ParsingProcess} & {@code
   * Results} in order to convert, parse and create the analysis results.
   *
   * <p>If -analyze flag is used, the {@code ConversionProcess} is skipped and the {@code
   * ParsingProcess} executes with the provided directory as its input.
   *
   * <p>If an error occurs it displays it's message and prompts the user with a choice to eather
   * keep the converted files or delete the created folder.
   *
   * @param args The command-line arguments passed to the program.
   */
  public void manageUserInput(String[] args) {
    printWelcomeMessage();

    if (args != null && args.length > 0 && args[0] != null) {
      validateArguments(args);
    }
    printHelpMessage();

    if ((apkPath == null || apkPath.isEmpty()) && !isAnalysisSpecified) {
      System.out.println("Supported file formats are - APK, ZIP, XAPK\n");
      System.out.println(
          "To start the Android APK analysis, please input the path to your android apk file"
              + "\r\n");
      apkPath = scanner.next();
    }

    if (apkPath != null) {
      if (!validateFilePath(apkPath)) {
        // Continue to prompt the user until a valid file path is provided
        while (!validateFilePath(apkPath)) {
          System.out.println("error: Invalid path to file. Please check if the path exists");
          System.out.println(
              "To start the Android APK analysis, please input the path to your android apk file"
                  + "\r\n");
          apkPath = scanner.next();
        }
      }

      if (!validateFileType()) {
        throw new IllegalArgumentException(
            "Unsupported file format - only APK, ZIP and XAPK files are supported");
      }
    }

    if (jsonPatternsPath == null) {
      System.out.println(
          "\nWould you like to provide a JSON file with custom dangerous patterns for the analysis?\n(yes/no)"
              + "\r\n");
      String userResponse = scanner.next();
      while (!"yes".equalsIgnoreCase(userResponse) && !"no".equalsIgnoreCase(userResponse)) {
        System.out.println(
            "\nExpected 'yes' or 'no', please provide one of the given options" + "\r\n");
        userResponse = scanner.next();
      }
      if ("yes".equalsIgnoreCase(userResponse)) {
        System.out.println(
            "\nPlease enter the path to the JSON file you would like to use for the analysis:"
                + "\r\n");
        jsonPatternsPath = scanner.next();
        if (!validateFilePath(jsonPatternsPath)) {
          // Continue to prompt the user until a valid file path is provided
          while (!validateFilePath(jsonPatternsPath)
              || !jsonPatternsPath.toLowerCase().endsWith(".json")) {
            if (validateFilePath(jsonPatternsPath)
                && !jsonPatternsPath.toLowerCase().endsWith(".json")) {
              System.out.println(
                  "error: Invalid file format. Please check if the file is a JSON file");
            } else {
              System.out.println("error: Invalid path to file. Please check if the path exists");
            }
            System.out.println(
                "\nPlease enter the path to the JSON file you would like to use for the analysis:"
                    + "\r\n");
            jsonPatternsPath = scanner.next();
          }
        }
        System.out.println("\nRunning with custom patterns inside provided json" + "\r\n");
      }
      if ("no".equalsIgnoreCase(userResponse)) {
        System.out.println("\nRunning with default database pattern list" + "\r\n");
      }
      /*
       * If user doesn't want to use a custom JSON file, we are leaving
       * jsonPatternsPath as null, so only patterns from database will be loaded
       */
    }
    if (jsonPatternsPath == null) {
      try {
        /* Create a hash from APK */
        fileHash = FileHashing.generateHashFromFile(apkPath, "SHA-256");
        if (DatabaseFetchAnalysis.checkIfHashExist(fileHash) & !forceAnalysis) {
          hashExist = true;
          if (DatabaseFetchAnalysis.isHashDateOlder(fileHash)) {
            System.out.println(
                "\nApplication was previously scanned with outdated patterns. Retrieve previous scan results? (yes/no)\n");
          } else {
            System.out.println(
                "\nApplication was already scanned, do you want to retrieve the results? (yes/no)\n");
          }

          askUserForResultsFromDatabase();
        }
      } catch (IOException e) {
        System.err.println("Error occurred while generating a hash from an APK: " + e.getMessage());
      }
    }

    if (isMultiThreadingOn == null && !isAnalysisSpecified) {
      System.out.println(
          "\nWould you like to use multi-threading to speed up the conversion process?\n(Takes up a lot of systems resources) (yes/no)"
              + "\r\n");
      askUserForMultiThreading();
    }

    try {
      if (!isAnalysisSpecified) {
        System.out.println(
            "Starting the conversion and analysis process, this may take a while...\n");
        timer.start();
        if (!verbose) {
          OutputSilencer.silenceOutput();
        }
        ConversionProcess.startConversion(apkPath, isMultiThreadingOn, outputPath);
        ParsingProcess.startParsing(analysisDirPath);
        if (!verbose) {
          OutputSilencer.restoreOutput();
        }
        System.out.println(
            "===================================================================================");
        Timer.printDuration("Total process", timer.stop());

        /* Save analysis results into a database if custom json is not specified */
        if (jsonPatternsPath == null) {
          InsertAnalysisResults.insertResults();
        }
      } else {
        System.out.println("Starting the analysis process, this may take a while...\n");
        if (!verbose) {
          OutputSilencer.silenceOutput();
        }
        ParsingProcess.startParsing(analysisDirPath);
        if (!verbose) {
          OutputSilencer.restoreOutput();
        }
      }
      Results.createResults(this);

    } catch (IOException | InterruptedException e) {
      System.err.println("Error occurred during the process: " + e.getMessage());
      if (outputPath != null) {
        askForConvertedFileDeletion();
      }
    } finally {
      scanner.close();

      if (isMultiThreadingOn != null) {
        shutdownExecutor();
      }
    }
  }

  /**
   * Manages the user interface for specifying the output location of a PDF file. This method asks
   * the user to specify if they want to save the PDF to the desktop or to a custom path. It then
   * validates the specified path and uses {@code PDFConverter} to convert a Markdown file to PDF
   * format at the chosen location.
   *
   * @param mdPath The file path of the Markdown file to be converted.
   */
  public void managePDFOutput(String mdPath) {
    try {
      if (!validateFilePath(mdPath)) {
        throw new FileNotFoundException("mdFile was not found");
      }

      File mdFile = new File(mdPath);
      String answer;

      // If no arguments were given, asks the user for the path, where they want to
      // save the pdf
      if (pdfOutputPath == null) {
        while (pdfOutputPath == null) {
          System.out.println("Would you like to save the PDF of results to desktop? (yes/no)");
          answer = scanner.next().trim().toLowerCase();

          if ("yes".equals(answer)) {
            pdfOutputPath = getDesktopPath();
          } else if ("no".equals(answer)) {
            System.out.println("Please enter a custom path:");
            pdfOutputPath = scanner.next().trim();

          } else {
            System.out.println("Wrong input - please answer with 'yes' or 'no'.");
            continue;
          }

          // Validates the directory path, whether it's desktop or custom
          while (!validateDirPath(pdfOutputPath)) {
            System.out.println("The path entered is invalid. Please enter a valid path:");
            pdfOutputPath = scanner.next().trim();
          }
        }
      }
      // PDFConverter is called to convert md file to pdf format
      PDFConverter.generate(mdFile, pdfOutputPath);
      System.out.println(
          "\nPDF report of conducted analysis saved in: " + PDFConverter.getPdfFilePath());
    } catch (FileNotFoundException ex) {
      System.err.println(ex.getMessage());
    } catch (IOException e) {
      System.err.println("IO Error: " + e.getMessage());
    }
  }

  private String getDesktopPath() {
    String userHome = System.getProperty("user.home");
    return userHome + File.separator + "Desktop";
  }

  /**
   * Validates the provided file path.
   *
   * @return true if the path exists and represents a file, false otherwise.
   */
  public static boolean validateFilePath(String path) {
    Path filePath = Paths.get(path);
    return Files.exists(filePath) && !Files.isDirectory(filePath);
  }

  private boolean validateDirPath(String path) {
    Path dirPath = Paths.get(path);
    return Files.exists(dirPath) && Files.isDirectory(dirPath);
  }

  private void askUserForMultiThreading() {

    String userResponse = scanner.next();

    if ("yes".equalsIgnoreCase(userResponse)) {
      isMultiThreadingOn = true;
      System.out.println("Running with multi-threading set to TRUE" + "\r\n");
    } else if ("no".equalsIgnoreCase(userResponse)) {
      isMultiThreadingOn = false;
      System.out.println("Running with multi-threading set to FALSE" + "\r\n");
    } else {
      System.out.println(
          "\nExpected 'yes' or 'no', please provide one of the given options" + "\r\n");
      askUserForMultiThreading();
    }
  }

  private void askUserForResultsFromDatabase() {

    String userResponse = scanner.next().trim().toLowerCase();

    if ("yes".equalsIgnoreCase(userResponse)) {
      isFetchDatabaseData = true;
      Results.createResults(this);
      System.exit(0);
    } else if ("no".equalsIgnoreCase(userResponse)) {
      System.out.println("Results will not be retrieved from the database" + "\r\n");
    } else {
      System.out.println(
          "\nExpected 'yes' or 'no', please provide one of the given options" + "\r\n");
      askUserForResultsFromDatabase();
    }
  }

  /**
   * Validates the file type based on its extension (APK, ZIP, XAPK).
   *
   * @return true if the file has a valid extension, false otherwise.
   */
  private boolean validateFileType() {
    return apkPath.toLowerCase().endsWith(".apk")
        || apkPath.toLowerCase().endsWith(".zip")
        || apkPath.toLowerCase().endsWith(".xapk");
  }

  /**
   * Get the path to the APK file.
   *
   * @return The path to the APK file.
   */
  public static String getApkPath() {
    return apkPath;
  }

  /**
   * @return A Path to a directory, that should be analyzed.
   */
  public static Path analysisDirPath() {
    return analysisDirPath;
  }

  /**
   * Get the path to the JSON file storing dangerous patterns.
   *
   * @return The path to the JSON file storing dangerous patterns.
   */
  public static String getJsonPatternsPath() {
    return jsonPatternsPath;
  }

  /**
   * This method parses the command-line arguments to set the APK file path and the multithreading
   * option as well as the verbose option. The APK file path can be provided without a specific
   * flag, and the multithreading option is set using the -mt flag. The PDF output path can be set
   * using the -outpdf flag. The APK extraction path can be set using the -outdir flag. If -analyze
   * flag is provided, only the parsing process of the program will execute, analysing the contents
   * of the provided directory.
   *
   * <p>Usage examples: sus.jar path/to/apk -mt -outpdf path/to/pdf/output/dir -outdir
   * path/to/extract/pdf -v
   *
   * <p>Usage examples for the -help flag: sus.jar -help
   *
   * @param args The command-line arguments passed to the program.
   * @throws IllegalArgumentException If an exeption occurs parsing given arguments
   */
  private void validateArguments(String[] args) {

    for (String arg : args) {
      if ("-help".equalsIgnoreCase(arg)) {
        printHelpManual();
        System.exit(0);
      }
    }
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-mt":
          isMultiThreadingOn = true;
          System.out.println("Running with multi-threading set to TRUE\n");
          break;
        case "-outpdf":
          if (i + 1 < args.length) {
            pdfOutputPath = args[i + 1];

            if (pdfOutputPath != null && !validateDirPath(pdfOutputPath)) {
              throw new IllegalArgumentException("Expected a valid path after -outpdf flag");
            }
            i++;
          } else {
            throw new IllegalArgumentException("Expected a path after -outpdf flag");
          }

          break;
        case "-outdir":
          if (i + 1 < args.length) {

            outputPath = Paths.get(args[i + 1]);

            if (outputPath != null && !validateDirPath(outputPath.toString())) {
              throw new IllegalArgumentException("Expected a valid path after -outdir flag");
            }
            i++;
          } else {
            throw new IllegalArgumentException("Expected a path after -outdir flag");
          }
          break;
        case "-analyze":
          if (i + 1 < args.length) {
            analysisDirPath = Paths.get(args[i + 1]);

            if (analysisDirPath != null && !validateDirPath(analysisDirPath.toString())) {
              throw new IllegalArgumentException("Expected a valid path after -analyze flag");
            }
            isAnalysisSpecified = true;
            i++;
          } else {
            throw new IllegalArgumentException("Expected a path after -analyze flag");
          }

          break;
        case "-forceAnalysis":
          forceAnalysis = true;
          break;
        case "-v":
          verbose = true;
          break;
        case "-json":
          if (i + 1 < args.length) {
            jsonPatternsPath = args[i + 1];

            if (jsonPatternsPath != null && !validateFilePath(jsonPatternsPath)) {
              throw new IllegalArgumentException("Expected a valid path after -json flag");
            } else if (!jsonPatternsPath.toLowerCase().endsWith(".json")) {
              throw new IllegalArgumentException("Expected a valid JSON file after -json flag");
            }
            i++;
          } else {
            throw new IllegalArgumentException("Expected a path after -json flag");
          }
          break;
        default:
          if (!args[i].startsWith("-")) {
            apkPath = args[i];
          }
          break;
      }
    }
    if (isAnalysisSpecified) {
      apkPath = null;
      outputPath = null;
      isMultiThreadingOn = null;
    }
  }

  /** The help menu method that prints all the flags for the user* */
  private void printHelpManual() {
    System.out.println(
        "Welcome to the help menu\n\n\n"
            + "Usage: flag [options]\n\n\n"
            + "Options:\n\n"
            + "  <PathToAPK>              Path to the APK file location (No flag needed)\n\n"
            + "  -mt                      Increases application conversion speed by utilizing available cores\n\n"
            + "                           of the CPU (Warning - uses a lot of resources)\n\n"
            + "  -outdir <Directory>      Specify the directory to store output files\n\n"
            + "  -outpdf <OutputPDF>      Specify the path to the end result PDF\n\n"
            + "  -v                       Toggles verbose output of conversion and parsing processes from OFF to ON\n\n"
            + "  -json                    Allows the user to input a path to a json file with custom dangerous patterns\n\n"
            + "  -analyze                 Specify directory to analyze instead of an APK\n\n"
            + "  -forceAnalysis           Force analysis of APK even if it exists in the database\n\n");
  }

  /** Simple method that prints a welcome message to the user */
  private void printWelcomeMessage() {
    System.out.println("===============================================================");
    System.out.println("Welcome to SuS, the Android APP analyzer\r\n\n");
  }

  private void printHelpMessage() {
    System.out.println("Use -help to see all the available flags\r\n");
  }

  /** Asks the user if they would like to delete the converted files. */
  private void askForConvertedFileDeletion() {
    while (true) {
      System.out.println("Would you like to delete the converted files? (yes/no)");
      String userResponse = scanner.next();

      if ("yes".equalsIgnoreCase(userResponse)) {
        try {
          DeleteDir.deleteDirectory(outputPath);
          System.out.println("Converted files have been deleted.");
        } catch (IOException ex) {
          System.err.println("Error deleting the converted files: " + ex.getMessage());
        }
        break;
      } else if ("no".equalsIgnoreCase(userResponse)) {
        break;
      } else {
        System.out.println("Invalid response. Please answer 'yes' or 'no'.");
      }
    }
  }

  /** Shuts down the thread executor service. */
  private void shutdownExecutor() {
    try {
      ThreadPoolManager.getInstance().shutdownExecutor();
    } catch (InterruptedException e) {
      System.err.println("Error occurred during shutdown of thread executor: " + e.getMessage());
    }
  }

  public static String getFileHash() {
    return fileHash;
  }

  public static Boolean getHashExist() {
    return hashExist;
  }

  public static boolean getIsFetchDatabaseDataTrue() {
    return isFetchDatabaseData;
  }
}
