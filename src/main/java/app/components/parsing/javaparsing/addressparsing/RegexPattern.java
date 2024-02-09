package app.components.parsing.javaparsing.addressparsing;

import java.util.regex.Pattern;

/**
 * Utility class for creating regular expression patterns.
 *
 * <p>This class provides static methods to create regex patterns for domain names, and domain names
 * with post-validation exclusions.
 */
public class RegexPattern {

  // Pattern for matching domain names.
  private static final String DOMAIN_NAME_PATTERN =
      "(?:(?:https?|http|ftp|sftp|ftps|ws|wss)://)?(?:acra\\.)?(?:[\\w-]+\\.)+[\\w-]{2,}(?::\\d+)?(?:/\\S*)?";

  // Final regex for excluding domains with specific keywords.
  private static final String POST_VALIDATION_DOMAIN_PATTERN =
      "^(?!.*\\b(android|androidx|androidx|googleapis|googleads|java|kotlin|vm|os|id|name|\\.run)\\b).*$";

  /**
   * @return Compiled {@link Pattern} for domain names.
   */
  public static Pattern getDomainNamePattern() {
    return Pattern.compile(DOMAIN_NAME_PATTERN);
  }

  /**
   * @return A {@link Pattern} for finding domain names in text, excluding specific keywords to
   *     prevent matching certain domains.
   */
  public static Pattern getPostValidationDomainPattern() {
    return Pattern.compile(POST_VALIDATION_DOMAIN_PATTERN);
  }
}
