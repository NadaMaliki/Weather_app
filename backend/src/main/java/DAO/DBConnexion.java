package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnexion implements AutoCloseable {
    private static final String DB_URL = "jdbc:mysql://localhost:3366/weather_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private Connection con;

    // Constructeur pour initialiser la connexion
    public DBConnexion() {
        // Connexion lazily, initialisation de la connexion sera effectuée dans getCon()
    }

    // Méthode pour obtenir la connexion, avec gestion automatique de la transaction
    public Connection getCon() throws SQLException {
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            con.setAutoCommit(false);  // Désactiver le mode autocommit pour les transactions
        }
        return con;
    }

    @Override
    public void close() {
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
