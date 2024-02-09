package app.components.parsing.javaparsing.detectors;

import app.components.model.FileInfo;
import app.utils.ReadDataFromJSON;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code IpDetector} class is responsible for detecting blacklisted IP addresses. It loads a
 * list of blacklisted IPs and their corresponding nodes from a JSON file and provides functionality
 * to check if IPs from a {@link FileInfo} object are blacklisted.
 */
public class IpDetector {
  private HashMap<String, String> blacklistedIps;

  /*Read all the blacklistedIps from the json into a HashMap */
  public IpDetector() {
    this.blacklistedIps = ReadDataFromJSON.getBlacklistedIPs();
  }

  /**
   * Checks if the given IP address is blacklisted.
   *
   * @param ip The IP address to check.
   * @return {@code true} if the IP address is blacklisted, {@code false} otherwise.
   */
  public boolean isIpBlacklisted(String ip) {
    return blacklistedIps.containsKey(ip);
  }

  /**
   * Checks IP addresses from a {@link FileInfo} object against the list of blacklisted IPs. If a
   * match is found, the IP and its corresponding node are added to the FileInfo's blacklisted IPs.
   *
   * @param fileInfo The {@link FileInfo} object containing IP addresses to be checked.
   */
  public void checkFileIps(FileInfo fileInfo) {
    /*Combine both ipv4 and ipv6 addresses for optimized checking */
    Set<String> combinedIPs = new HashSet<>();
    combinedIPs.addAll(fileInfo.getIpv4Addresses());
    combinedIPs.addAll(fileInfo.getIpv6Addresses());
    /*Check if the IP is blacklisted*/
    for (String ip : combinedIPs) {
      if (isIpBlacklisted(ip)) {
        String node = blacklistedIps.get(ip);
        fileInfo.addBlacklistedIP(ip, node);
      }
    }
  }
}
