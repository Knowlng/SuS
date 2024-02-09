package app.conversion;

import static org.junit.jupiter.api.Assertions.*;

import app.components.conversion.classconverter.ClassConverter;
import app.components.conversion.dexconverter.Dex2JarExecutor;
import app.components.conversion.unzip.UnzipFile;
import app.components.conversion.xmlconverter.XMLConverter;
import app.utils.ReadFilesFromDirectory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConversionIT {

  @Test
  void testCompleteConversionProcess(@TempDir Path tempDir) throws Exception {
    Path apkFilePath = Paths.get("src/integrationTest/resources/apk/test.apk");

    Path unzippedPath = UnzipFile.unzip(apkFilePath.toString(), tempDir.toString());

    Path binaryXmlPath = unzippedPath.resolve("AndroidManifest.xml");
    String xmlContent = XMLConverter.convertToXmlString(binaryXmlPath.toString());
    // verify if XMLConverter returns a non empty String
    assertNotNull(xmlContent, "Conversion of binary XML to plain text XML failed");

    Dex2JarExecutor.convertToJar(unzippedPath, false);

    ClassConverter.convert(Dex2JarExecutor.getOutputDir(), unzippedPath, false);

    // verify if JAVA directory exists and contains JAVA files
    Path javaSourcePath = unzippedPath.resolve(ClassConverter.getOutputFolderName());
    assertTrue(Files.exists(javaSourcePath), "Java source code directory does not exist");

    List<File> javaFiles =
        ReadFilesFromDirectory.getSpecificFilesFromDirectory(javaSourcePath, ".java");
    assertFalse(javaFiles.isEmpty(), "No Java files were created in the expected folder");
  }
}
