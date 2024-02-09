package app.components.model;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents information about a file, including IP addresses, domain names, and blacklisted IPs.
 * This class stores various details extracted from a file, such as IPv4 and IPv6 addresses, domain
 * names, and a map of blacklisted IP addresses along with their corresponding nodes.
 */
public class FileInfo {
  private File file;
  private String filename;
  private Set<String> ipv4Addresses = new HashSet<>();
  private Set<String> ipv6Addresses = new HashSet<>();
  private Set<String> domains = new HashSet<>();
  private HashMap<String, String> blacklistedIps = new HashMap<>();

  // Constructors
  public FileInfo(File file) {
    this.file = file;
  }

  public FileInfo() {}

  // Getters and Setters
  public File getFile() {
    return file;
  }

  public String getFileName() {
    return file != null ? file.getName() : filename;
  }

  public void setFileName(String filename) {
    this.filename = filename;
  }

  public Set<String> getIpv4Addresses() {
    return ipv4Addresses;
  }

  public Set<String> getIpv6Addresses() {
    return ipv6Addresses;
  }

  public Set<String> getDomains() {
    return domains;
  }

  public Map<String, String> getBlacklistedIPs() {
    return blacklistedIps;
  }

  // Methods to add IPs and domains
  public void addIpv4Address(String ipAddress) {
    this.ipv4Addresses.add(ipAddress);
  }

  public void addIpv6Address(String ipAddress) {
    this.ipv6Addresses.add(ipAddress);
  }

  public void addDomainName(String domain) {
    this.domains.add(domain);
  }

  public void addBlacklistedIP(String ipAddress, String node) {
    this.blacklistedIps.put(ipAddress, node);
  }

  // Method to return a string representation of the file information
  // Returns a string representation of the file information, including file name,
  // domains, and IP
  // addresses.
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("* FileName: ").append(getFileName()).append("\n\n");

    if (!domains.isEmpty()) {
      sb.append("- Domains: ").append(String.join(", ", domains)).append("\n\n");
    }

    if (!ipv4Addresses.isEmpty()) {
      sb.append("- IPv4 Addresses: ").append(String.join(", ", ipv4Addresses)).append("\n\n");
    }

    if (!ipv6Addresses.isEmpty()) {
      sb.append("- IPv6 Addresses: ").append(String.join(", ", ipv6Addresses)).append("\n\n");
    }

    if (!blacklistedIps.isEmpty()) {
      sb.append("- Blacklisted IP addresses and their hosts: ");
      blacklistedIps.forEach(
          (ip, node) -> sb.append("{").append(ip).append(", ").append(node).append("}, "));

      if (blacklistedIps.size() > 0) {
        sb.setLength(sb.length() - 2); // removes the trailing comma and space at the end
      }

      sb.append("\n\n");
    }

    return sb.toString();
  }
}
