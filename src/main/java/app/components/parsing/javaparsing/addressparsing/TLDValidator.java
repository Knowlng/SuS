package app.components.parsing.javaparsing.addressparsing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
 * Utility class for validating top-level domains(TLDs)
 * This class provides functionality to check the validity of a domain's TLD against a list of predefined valid TLDs
 */
public class TLDValidator {
  private static final Set<String> VALID_TLDS =
      new HashSet<>(
          Arrays.asList(
              "com", "org", "net", "edu", "gov", "uk", "de", "ca", "pl", "lt", "ir", "co.uk",
              "com.br", "icu", "dev", "ads", "Ads", "cn", "ru", "nl", "br", "au", "fr", "it",
              "onion"));

  /**
   * Checks if the provided domain has a valid top-level domain (TLD).
   *
   * @param domain The domain string to validate.
   * @return {@code true} if the domain's TLD is valid, {@code false} otherwise.
   */
  public static boolean isValidTLD(String url) {
    try {
      // Extract the domain from the URL
      String domain = new URI(url).getHost();
      if (domain == null || domain.isEmpty()) {
        return false;
      }

      // Split the domain to get the TLD
      String[] parts = domain.split("\\.");
      String tld = parts[parts.length - 1].toLowerCase();
      return VALID_TLDS.contains(tld);
    } catch (URISyntaxException e) {
      return false; // Invalid URL format
    }
  }
}
