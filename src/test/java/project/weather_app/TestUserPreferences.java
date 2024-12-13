package project.weather_app;

import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserPreferences {

    private UserPreferencesManager dbManager;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialiser une connexion à une base de données en mémoire (H2)
        String url = "jdbc:h2:mem:testdb"; // Base de données en mémoire
        connection = DriverManager.getConnection(url, "sa", "");

        // Créer une instance personnalisée de DBConnexion
        DBConnexion testDbConnexion = new DBConnexion() {
            @Override
            public Connection getCon() {
                return connection; // Retourne la connexion à H2
            }
        };

        // Injecter l'instance personnalisée dans userpreferencesManager
        dbManager = new UserPreferencesManager(testDbConnexion);

        // Initialiser les tables et les données de test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE preferences (" +
                    "Id_utilisateur INT PRIMARY KEY, " +
                    "Ville1 VARCHAR(255), Ville2 VARCHAR(255), Ville3 VARCHAR(255), " +
                    "Ville4 VARCHAR(255), Ville5 VARCHAR(255), " +
                    "Unite VARCHAR(50) DEFAULT '°C', " +
                    "Ville_par_Defaut VARCHAR(50), " +
                    "Alerte BOOLEAN DEFAULT FALSE);");
            stmt.execute("INSERT INTO preferences (Id_utilisateur, Ville1, Unite, Ville_par_Defaut, Alerte) " +
                    "VALUES (1, 'Paris', '°C', 'Casablanca', TRUE);");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE preferences;");
            }
            connection.close();
        }
    }

    //testes de l'attribut unite
    @Test
    void testGetUnit() {
        // Tester la méthode
        String unit = dbManager.getUnit(1);
        assertEquals("°C", unit, "L'unité par défaut doit être '°C'.");
    }

    @Test
    void testSetUnit() {
        boolean result = dbManager.updateUnit(1, "°F");
        assertTrue(result, "La mise à jour de l'unité doit réussir.");

        String updatedUnit = dbManager.getUnit(1);
        assertEquals("°F", updatedUnit, "L'unité doit être '°F'.");
    }

    //testes des methodes de l'attribut ville_par_defaut
    @Test
    void testGetDefaultCity() {
        String city = dbManager.getDefaultCity(1);
        assertEquals("Casablanca", city, "La ville par défaut doit être 'Casablanca'.");
    }

    @Test
    void testEditDefaultCity() {
        boolean result = dbManager.editDefaultCity(1, "Lyon");
        assertTrue(result, "La mise à jour doit réussir.");

        String updatedCity = dbManager.getDefaultCity(1);
        assertEquals("Lyon", updatedCity, "La ville par défaut doit être 'Lyon'.");
    }

    //testes des methodes de l'attribut alerte
    @Test
    void testIsAlertActive() {
        // Insérer une valeur d'alerte dans la base de données
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE preferences SET Alerte = TRUE WHERE Id_utilisateur = 1");
        } catch (SQLException e) {
            fail("Erreur lors de la configuration des données : " + e.getMessage());
        }

        boolean isActive = dbManager.getAlerte(1);
        assertTrue(isActive, "L'alerte doit être active.");
    }


    @Test
    void testSetAlertActive() {
        boolean result = dbManager.setAlerte(1, false);
        assertTrue(result, "La mise à jour de l'alerte doit réussir.");

        boolean isActive = dbManager.getAlerte(1);
        assertFalse(isActive, "L'alerte doit être désactivée.");
    }

    //testes des methodes de l'attribut FavoriteCities
    @Test
    void testGetFavoriteCities() {
        List<String> cities = dbManager.getFavoriteCities(1);
        assertEquals(1, cities.size(), "L'utilisateur doit avoir 1 ville favorite.");
        assertTrue(cities.contains("Paris"), "La ville favorite doit être 'Paris'.");
    }

    @Test
    void testAddFavoriteCity() {
        boolean result = dbManager.addFavoriteCity(1, "New York");
        assertTrue(result, "L'ajout de la ville favorite doit réussir.");
        List<String> cities = dbManager.getFavoriteCities(1);
        assertEquals(2, cities.size(), "L'utilisateur doit avoir 2 villes favorites.");
        assertTrue(cities.contains("New York"), "La ville favorite doit inclure 'New York'.");
    }

    @Test
    void testUpdateFavoriteCity() {
        boolean result = dbManager.updateFavoriteCity(1, 1, "Tokyo");
        assertTrue(result, "La modification de la ville favorite doit réussir.");
        List<String> cities = dbManager.getFavoriteCities(1);
        assertEquals("Tokyo", cities.get(0), "La première ville favorite doit être 'Tokyo'.");
    }

    @Test
    void testRemoveFavoriteCity() {
        boolean result = dbManager.removeFavoriteCity(1, 1);
        assertTrue(result, "La suppression de la ville favorite doit réussir.");
        List<String> cities = dbManager.getFavoriteCities(1);
        assertTrue(cities.isEmpty(), "Il ne doit plus y avoir de villes favorites.");
    }


}

