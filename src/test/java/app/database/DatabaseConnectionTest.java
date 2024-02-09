package app.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.database.databaseConnection.DatabaseConnection;
import java.sql.Connection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DatabaseConnectionTest {

  @Test
  @Disabled
  public void testConnectNotNull() throws Exception {
    Connection connection = DatabaseConnection.connect();
    assertNotNull(connection, "Connection should not be null");

    if (connection != null) {
      connection.close();
    }
  }
}
