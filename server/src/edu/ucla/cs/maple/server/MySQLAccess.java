package edu.ucla.cs.maple.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;



public class MySQLAccess {
    final String url = "jdbc:mysql://localhost:3306/maple";
    final String username = "root";
    final String password = "Password69";
    String table;
    Connection connect = null;
    Statement statement = null;
    public ResultSet result = null;
    PreparedStatement prep = null;
    
    public void connect() {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<String> getPatterns(String _class, String _method) {
        ArrayList<String> patterns = new ArrayList<String>();
        
        if (connect != null) {
            try {
                // construct the query
                String query = "select pattern from patterns where class='"
                        + _class + "' and method='" + _method + "';";
                
                prep = connect.prepareStatement(query);
                result = prep.executeQuery();
                while(result.next()) {
                    // populate ArrayList with the results
                    patterns.add(result.getString("pattern"));
                }
                result.close();
                
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        
        return patterns;
    }

    public void close() {
        try {
            if (result != null)
                result.close();
            if (statement != null)
                statement.close();
            if (prep != null)
                prep.close();
            if (connect != null)
                connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
