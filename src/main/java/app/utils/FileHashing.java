package app.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashing {

  private static final int BUFFER_SIZE = 4096;

  /**
   * Generates a hash from a file using a specific algorithm.
   *
   * @param filePath the path to the file
   * @param algorithm the hash algorithm to use ("SHA-256")
   * @return the hash of the file
   * @throws IOException if an I/O error occurs
   */
  public static String generateHashFromFile(String filePath, String algorithm) throws IOException {
    if (algorithm == null || algorithm.isEmpty()) {
      throw new IOException("Algorithm cannot be null or empty");
    }
    if (filePath == null || filePath.isEmpty()) {
      throw new IOException("File path canont be null or empty");
    }

    try {
      MessageDigest digest = MessageDigest.getInstance(algorithm);
      try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
        byte[] byteArray = new byte[BUFFER_SIZE];
        int bytesCount;
        while ((bytesCount = fileInputStream.read(byteArray)) != -1) {
          digest.update(byteArray, 0, bytesCount);
        }
      }
      byte[] hashBytes = digest.digest();
      return bytesToHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Algorithm: " + algorithm + " is unsupported");
    }
  }

  /**
   * Converts a byte array to a hexadecimal string. Reffers
   * https://www.baeldung.com/sha-256-hashing-java
   *
   * @param bytes the byte array to convert
   * @return the hexadecimal string
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
