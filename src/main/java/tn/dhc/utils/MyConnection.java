package tn.dhc.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    Connection connection;

    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/dhc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static MyConnection instance;

    private MyConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion Etablie!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
