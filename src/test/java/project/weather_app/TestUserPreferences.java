package project.weather_app;

import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestUserPreferences {

    private UserPreferencesManager dbManager;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialiser une connexion à une base de données en mémoire (H2)
        String url = "jdbc:h2:mem:testdb"; // Base de données en mémoire
        String user = "sa";
        String pass = "";

        // Créer une instance personnalisée de DBConnexion
        DBConnexion testDbConnexion = new DBConnexion();

        // Injecter l'instance personnalisée dans userpreferencesManager
        dbManager = new UserPreferencesManager(testDbConnexion);
        connection= dbManager.getContest(url,user,pass);


        // Initialiser les tables et les données de test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE preferences (" +
                    "Id INT PRIMARY KEY, " +
                    "Id_utilisateur INT, " +
                    "Ville1 VARCHAR(255), Ville2 VARCHAR(255), Ville3 VARCHAR(255), " +
                    "Ville4 VARCHAR(255), Ville5 VARCHAR(255), " +
                    "Unite VARCHAR(50) DEFAULT '°C', " +
                    "Ville_par_Defaut VARCHAR(50) );");

            stmt.execute("INSERT INTO preferences (Id, Id_utilisateur, Ville1, Unite, Ville_par_Defaut) " +
                    "VALUES (1, 1, 'Paris', '°C', 'Casablanca');");

            stmt.execute("CREATE TABLE utilisateur (" +
                    "Id INT PRIMARY KEY, " +
                    "nom VARCHAR(255), " +
                    "prenom VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "password VARCHAR(255), " +
                    "Alerte int );");

            stmt.execute("INSERT INTO utilisateur (Id, nom, prenom, email, password, Alerte) " +
                    "VALUES (1, 'john', 'john', 'email@gmail.com', '', 0);");
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


    @Test
    public void testConvertTemperature() {

        // Test conversion de Celsius à Fahrenheit
        assertEquals(32, dbManager.convertTemperature(0, "°F"), 0.01);

        // Test conversion avec valeur par défaut (Celsius)
        assertEquals(0, dbManager.convertTemperature(0, "°C"), 0.01);

        // Test conversion avec une température positive
        assertEquals(298.15, dbManager.convertTemperature(25, "°K"), 0.01);
        assertEquals(77, dbManager.convertTemperature(25, "°F"), 0.01);

        // Test conversion avec une température négative
        assertEquals(193.15, dbManager.convertTemperature(-80, "°K"), 0.01);
        assertEquals(-112, dbManager.convertTemperature(-80, "°F"), 0.01);
    }
}




