package java;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.http.HttpServletRequest;

public class SQLInjectionExample {

    public void doGet(HttpServletRequest request, Connection connection) throws Exception {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
    }
}