package app.components.parsing.javaparsing.addressparsing;

import app.components.model.FileInfo;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * This class processes a map of files to their corresponding string contents, identifies valid
 * domain names, IPv4 and IPv6 addresses, and compiles this information into a list of {@link
 * FileInfo} objects.
 */
public class FileAnalyzer {
  /**
   * Processes a map of files to lists of strings, extracting valid domain names and IP addresses.
   *
   * <p>Domain names are validated using a 3 level validation approach:
   *
   * <p>1. Check domain with regex 2. Use TLDValidator to check the domains with already valid TLDs
   * 3. Use the post validation regex to exclude domains which contain implied safe keywords such as
   * java, or android, androidx
   *
   * @param fileToStringsMap A map where each key is a {@link File} object and the value is a list
   *     of strings.
   * @return A list of {@link FileInfo} objects, each representing a file and the extracted
   *     information.
   * @throws IllegalArgumentException If the input map is null.
   */
  public static List<FileInfo> processStrings(Map<File, List<String>> fileToStringsMap) {

    if (fileToStringsMap == null) {
      throw new IllegalArgumentException("Input map is null");
    }

    List<FileInfo> fileInfoList = new ArrayList<>();
    Pattern domainPattern = RegexPattern.getDomainNamePattern();
    Pattern postValidationDomainPattern = RegexPattern.getPostValidationDomainPattern();
    InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();

    for (Map.Entry<File, List<String>> entry : fileToStringsMap.entrySet()) {

      File file = entry.getKey();
      List<String> strings = entry.getValue();
      Set<String> validDomains = new HashSet<>();
      Set<String> validIPv4s = new HashSet<>();
      Set<String> validIPv6s = new HashSet<>();
      // Use the 3 level approach
      for (String str : strings) {
        if (domainPattern.matcher(str).matches() && TLDValidator.isValidTLD(str)) {
          validDomains.add(str);
        } else if (inetAddressValidator.isValidInet4Address(str) && !str.equals("...")) {
          validIPv4s.add(str);
        } else if (inetAddressValidator.isValidInet6Address(str) && !str.equals("::")) {
          validIPv6s.add(str);
        }
      }
      // With the 3th level exclude the domains with keywoards.
      validDomains.removeIf(domain -> !postValidationDomainPattern.matcher(domain).matches());
      // Add domains to FileInfo object, which can only exist if there are contents
      // within it.
      if (!validDomains.isEmpty() || !validIPv4s.isEmpty() || !validIPv6s.isEmpty()) {
        FileInfo fileInfo = new FileInfo(file);
        validDomains.forEach(fileInfo::addDomainName);
        validIPv4s.forEach(fileInfo::addIpv4Address);
        validIPv6s.forEach(fileInfo::addIpv6Address);
        fileInfoList.add(fileInfo);
      }
    }
    return fileInfoList;
  }
}
