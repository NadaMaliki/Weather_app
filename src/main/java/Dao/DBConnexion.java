package Dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnexion {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/weather_app2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static Connection con;

    // Constructeur pour initialiser la connexion
    public DBConnexion() {
        // Connexion lazy, initialisation de la connexion sera effectuée dans getCon()
    }

    // Méthode pour obtenir la connexion
    public static Connection getCon() throws SQLException {
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            con.setAutoCommit(false);  // Désactiver le mode autocommit pour les transactions
        }
        return con;
    }

    // Méthode de fermeture de la connexion
    public void closeConnection() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }
}