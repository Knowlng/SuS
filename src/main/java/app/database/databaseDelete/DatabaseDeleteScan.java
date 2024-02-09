package app.database.databaseDelete;

import app.database.databaseConnection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDeleteScan {
  public static void deleteLastHashResult(String hash) {
    List<Integer> detectedPatternIds = getDetectedPatternIds(hash);

    for (Integer id : detectedPatternIds) {
      deleteFoundObjectData(id);
      deleteEdges(id);
      deleteDetectedPatterns(id);
    }
    deleteHashPermissions(hash);
    deleteHashXmlPatterns(hash);
  }

  private static void deleteFoundObjectData(int detectedPatternId) {
    String deleteQuery = "DELETE FROM foundobjectdata WHERE detectedpatternid = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

      preparedStatement.setInt(1, detectedPatternId);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.out.println("Error occurred while deleting data: " + e.getMessage());
    }
  }

  private static void deleteEdges(int detectedPatternId) {
    String deleteQuery = "DELETE FROM edge WHERE detectedpatternid = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

      preparedStatement.setInt(1, detectedPatternId);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.out.println("Error occurred while deleting data: " + e.getMessage());
    }
  }

  private static void deleteDetectedPatterns(int detectedPatternId) {
    String deleteQuery = "DELETE FROM detectedPatterns WHERE detectedpatternid = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

      preparedStatement.setInt(1, detectedPatternId);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      System.out.println("Error occurred while deleting data: " + e.getMessage());
    }
  }

  private static List<Integer> getDetectedPatternIds(String hash) {
    List<Integer> ids = new ArrayList<>();
    String selectQuery = "SELECT detectedpatternid FROM detectedPatterns WHERE hashvalue = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

      preparedStatement.setString(1, hash);
      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        ids.add(resultSet.getInt("detectedpatternid"));
      }
    } catch (SQLException e) {
      System.out.println("Error occurred while fetching data: " + e.getMessage());
    }

    return ids;
  }

  private static void deleteHashPermissions(String hash) {
    final String deleteQuery = "DELETE FROM hashes_permissions WHERE hashvalue = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

      preparedStatement.setString(1, hash);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Error occurred while deleting data: " + e.getMessage());
    }
  }

  private static void deleteHashXmlPatterns(String hash) {
    final String deleteQuery = "DELETE FROM hashes_xmlpatterns WHERE hashvalue = ?";

    try (Connection connection = DatabaseConnection.connect();
        PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

      preparedStatement.setString(1, hash);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Error occurred while deleting data: " + e.getMessage());
    }
  }
}
