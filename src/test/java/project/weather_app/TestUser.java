package project.weather_app;



import Model.User;
import  Dao.DatabaseManager;
import  Dao.DBConnexion;
import lombok.SneakyThrows;

import java.sql.*;

public class TestUser {

    private static DatabaseManager databaseManager;
    private static Connection realConnection;

    public static void main(String[] args) {
        System.out.println("== Test de la classe User ==");

        // Configuration de la base de données en mémoire H2
        try {
            setUpRealDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 1. Test du constructeur avec tous les paramètres
        User user1 = new User(1, "aabir", "John", "john.doe@example.com", "password123", 0);
        System.out.println("Test constructeur complet:");
        printUserDetails(user1);

        // 2. Test du constructeur pour les nouveaux utilisateurs (sans ID)
        User user2 = new User("JaneDoe", "Doe", "Jane", "jane.doe@example.com", "password456");
        System.out.println("\nTest constructeur pour nouveaux utilisateurs:");
        printUserDetails(user2);

        // 3. Test ajout utilisateur avec commit
        addUserAndCommit(user2);
    }

    // Méthode utilitaire pour afficher les détails d'un utilisateur
    private static void printUserDetails(User user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Nom: " + user.getNom());
        System.out.println("Prénom: " + user.getPrenom());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Password: " + user.getPassword());
        System.out.println("Alerte: " + user.isAlerte());

    }

    // Configuration de la base de données en mémoire
    @SneakyThrows
    private static void setUpRealDatabase() throws SQLException {
        // Connexion à une base de données en mémoire H2 pour les tests
        realConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        Statement stmt = realConnection.createStatement();
        stmt.execute("CREATE TABLE utilisateur (Id INT AUTO_INCREMENT PRIMARY KEY, Nom VARCHAR(255), Prenom VARCHAR(255), Email VARCHAR(255), Password VARCHAR(255), Alerte BOOLEAN, Preferences_Id INT, Historique_Id INT)");

        // Désactive le mode auto-commit
        realConnection.setAutoCommit(false);

        databaseManager = new DatabaseManager(new DBConnexion());
    }


    // Méthode pour ajouter un utilisateur et simuler un commit
    private static void addUserAndCommit(User user) {
        try {
            // Préparer l'insertion de l'utilisateur dans la base de données réelle
            String query = "INSERT INTO utilisateur (Nom, Prenom, Email, Password, Alerte, Preferences_Id, Historique_Id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = realConnection.prepareStatement(query)) {
                stmt.setString(1, user.getNom());
                stmt.setString(2, user.getPrenom());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, user.getPassword());
                stmt.setInt(5, user.isAlerte());

                int result = stmt.executeUpdate();
                System.out.println("Ajout utilisateur: " + (result > 0));

                // Commit transaction
                realConnection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

