

import Dao.DBConnexion;
import Dao.DatabaseManager;
import Dao.UserPreferencesManager;
import  Model.SearchHistory;
import Model.User;
import org.junit.jupiter.api.*;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DatabaseManagerTest {
    private DatabaseManager databaseManager;
    private DBConnexion dbConnexion;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialisation de la connexion et de DatabaseManager
        dbConnexion = new DBConnexion();
        databaseManager = new DatabaseManager(dbConnexion);

        // Nettoyage de la base de données avant chaque test
        removeUserByEmail("john.doe@example.com");
    }

    private void removeUserByEmail(String email) throws SQLException {
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM utilisateur WHERE Email = ?")) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    @Test
    void testAddUser() throws SQLException {
        // Création d'un utilisateur
        User user = new User(2, "Doe", "John", "john.doe@example.com", "password123", 0);

        // Appel de la méthode addUser
        int generatedId = databaseManager.addUser(user);
        assertTrue(generatedId > 0, "L'utilisateur doit être ajouté avec succès, et un ID doit être généré.");

        // Vérification dans la base de données
        try (Connection conn = dbConnexion.getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM utilisateur WHERE Email = 'john.doe@example.com'")) {

            assertTrue(rs.next(), "L'utilisateur doit exister dans la base de données");
            assertEquals("John", rs.getString("Prenom"), "Le prénom doit être 'John'");
            assertEquals("Doe", rs.getString("Nom"), "Le nom doit être 'Doe'");
            assertEquals("john.doe@example.com", rs.getString("Email"), "L'email doit être 'john.doe@example.com'");
        }
    }


    @Test
    void testGetUserById() throws SQLException {
        // Préparer un utilisateur dans la base de données
        User user = new User(2, "Doe", "John", "john.doe@example.com", "password123",  0);
        databaseManager.addUser(user);

        // Récupérer l'ID de l'utilisateur
        try (Connection conn = dbConnexion.getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id FROM utilisateur WHERE Email = 'john.doe@example.com'")) {
            assertTrue(rs.next(), "L'utilisateur doit exister dans la base de données");
            int userId = rs.getInt("Id");
            System.out.println("User ID récupéré : " + userId);

            // Tester la méthode
            User retrievedUser = databaseManager.getUserById(userId);
            assertNotNull(retrievedUser, "L'utilisateur récupéré ne doit pas être null");
            assertEquals("John", retrievedUser.getPrenom(), "Le prénom doit être 'John'");
            assertEquals("Doe", retrievedUser.getNom(), "Le nom doit être 'Doe'");
        }
    }
    @Test
    void testGetUserByEmail() throws SQLException {
        String testEmail = "jane.doe@example.com";

        // Étape 1 : Créer et ajouter un utilisateur pour le test
        User testUser = new User(3, "Doe", "Jane", testEmail, "securePassword", 1);
        int generatedId = databaseManager.addUser(testUser);
        assertTrue(generatedId > 0, "L'utilisateur doit être ajouté avec succès et un ID doit être généré.");

        // Étape 2 : Utiliser la méthode getUserByEmail pour récupérer l'utilisateur
        User retrievedUser = databaseManager.getUserByEmail(testEmail);
        assertNotNull(retrievedUser, "L'utilisateur doit être correctement trouvé dans la base de données.");

        // Assertions pour vérifier les valeurs des champs
        assertEquals(testUser.getEmail(), retrievedUser.getEmail(), "Les adresses email doivent correspondre.");
        assertEquals(testUser.getNom(), retrievedUser.getNom(), "Les noms doivent correspondre.");
        assertEquals(testUser.getPrenom(), retrievedUser.getPrenom(), "Les prénoms doivent correspondre.");
        assertEquals(testUser.getAlerte(), retrievedUser.getAlerte(), "Les valeurs d'alerte doivent correspondre.");

        // Étape 3 : Validation des valeurs dans la base de données
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM utilisateur WHERE email = ?")) {
            stmt.setString(1, testEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "L'utilisateur doit être présent dans la base de données.");
                assertEquals(testUser.getEmail(), rs.getString("Email"), "L'email doit correspondre.");
                assertEquals(testUser.getNom(), rs.getString("Nom"), "Le nom doit correspondre.");
                assertEquals(testUser.getPrenom(), rs.getString("Prenom"), "Le prénom doit correspondre.");
            }
        }
    }



    @Test
    void testUpdateUser() throws SQLException {
        // Ajouter un utilisateur
        User user = new User(2, "Doe", "John", "john.doe@example.com", "password123", 0);
        databaseManager.addUser(user);

        // Récupérer l'utilisateur ajouté
        int userId;
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement("SELECT Id FROM utilisateur WHERE Email = ?")) {
            stmt.setString(1, "john.doe@example.com");
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "L'utilisateur doit exister dans la base de données");
                userId = rs.getInt("Id");
            }
        }
        // Mettre à jour les informations de l'utilisateur
        user.setId(userId);
        user.setNom("Jane");
        user.setPrenom("Doe");
        user.setEmail("jane.doe@example.com");

        boolean isUpdated = databaseManager.updateUser(user);
        assertTrue(isUpdated, "L'utilisateur doit être mis à jour");

        // Vérifier les mises à jour dans la base de données
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM utilisateur WHERE Id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "L'utilisateur doit toujours exister");
                assertEquals("Jane", rs.getString("Nom"), "Le nom doit être 'Jane'");
                assertEquals("Doe", rs.getString("Prenom"), "Le prénom doit être 'Doe'");
                assertEquals("jane.doe@example.com", rs.getString("Email"), "L'email doit être 'jane.doe@example.com'");
            }
        }
    }

    @Test
    void testDeleteUser() throws SQLException {
        // Ajouter un utilisateur
        User user = new User(0, "John", "Doe", "john.doe@example.com", "password123");
        databaseManager.addUser(user);

        // Récupérer l'ID de l'utilisateur
        int userId;
        try (Connection conn = dbConnexion.getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id FROM utilisateur WHERE Email = 'john.doe@example.com'")) {
            assertTrue(rs.next(), "L'utilisateur doit exister dans la base de données");
            userId = rs.getInt("Id");
        }

        // Supprimer l'utilisateur
        boolean isDeleted = databaseManager.deleteUser(userId);
        assertTrue(isDeleted, "L'utilisateur doit être supprimé");

        // Vérifier qu'il n'existe plus
        try (Connection conn = dbConnexion.getCon();
             Statement stmt = conn.createStatement();
             ResultSet deletedRs = stmt.executeQuery("SELECT * FROM utilisateur WHERE Id = " + userId)) {
            assertFalse(deletedRs.next(), "L'utilisateur ne doit plus exister dans la base de données");
        }
    }

    @Test
    void testAddUserAndIsUserAuthenticated_Success() throws SQLException {
        // Création d'un utilisateur pour le test
        String email = "john.doe@example.com";
        String plainPassword = "password123";

        // Utilisation de la méthode addUser pour ajouter l'utilisateur à la base de données
        User user = new User(2, "Doe", "John", email, plainPassword, 0);
        int generatedId = databaseManager.addUser(user);

        // Vérifie que l'utilisateur a été ajouté avec succès (ID généré positif)
        assertTrue(generatedId > 0, "L'utilisateur doit être ajouté avec succès");

        // Vérification de l'authentification avec le mot de passe correct
        boolean isAuthenticated = databaseManager.isUserAuthenticated(email, plainPassword);
        assertTrue(isAuthenticated, "L'utilisateur doit être authentifié avec un mot de passe valide");
    }

    @Test
    void testIsUserAuthenticated_Failure() throws SQLException {
        // Création d'un utilisateur pour le test
        String email = "john.doe@example.com";
        String plainPassword = "password123";
        String wrongPassword = "wrongPassword";

        // Utilisation de la méthode addUser pour ajouter l'utilisateur à la base de données
        User user = new User(2, "Doe", "John", email, plainPassword, 0);
        int generatedId = databaseManager.addUser(user);

        // Vérifie que l'utilisateur a été ajouté avec succès (ID généré positif)
        assertTrue(generatedId > 0, "L'utilisateur doit être ajouté avec succès");

        // Vérification de l'authentification avec un mot de passe incorrect
        boolean isAuthenticated = databaseManager.isUserAuthenticated(email, wrongPassword);
        assertFalse(isAuthenticated, "L'utilisateur ne doit pas être authentifié avec un mot de passe invalide");
    }


    @Test
    void testIsUserAuthenticated_NoUserFound() throws SQLException {
        // Appel de la méthode isUserAuthenticated pour un utilisateur non existant
        String email = "non.existent@example.com";
        String password = "password123";

        boolean isAuthenticated = databaseManager.isUserAuthenticated(email, password);
        assertFalse(isAuthenticated, "La méthode doit retourner false si l'utilisateur n'existe pas dans la base de données");
    }


    @Test
    public void testAddSearchHistory() {
        try {
            // Étape 1 : Créer un utilisateur pour le test
            User user = new User(109, "De", "Jhn", "john.oe@example.com", "pasword123", 0);
            int generatedId = databaseManager.addUser(user);
            assertTrue(generatedId > 0, "L'utilisateur devrait être ajouté avec succès.");
            System.out.println("Utilisateur ajouté : " + generatedId);  // Debugging line

            // Étape 2 : Récupérer l'utilisateur par ID pour obtenir ses informations
            User retrievedUser = databaseManager.getUserById(generatedId);

            // Debugging line to check the user information
            System.out.println("Utilisateur récupéré : " + retrievedUser);

            assertNotNull(retrievedUser, "L'utilisateur récupéré ne devrait pas être nul.");
            int userId = retrievedUser.getId();
            assertNotEquals(0, userId, "L'ID de l'utilisateur ne devrait pas être 0.");
            System.out.println("ID utilisateur récupéré : " + userId); // Debugging line

            // Étape 3 : Ajouter un historique de recherche pour cet utilisateur
            boolean searchHistoryAdded = databaseManager.addSearchHistory(userId);
            assertTrue(searchHistoryAdded, "L'historique de recherche devrait être ajouté avec succès.");

            // Étape 4 : Vérifier que l'historique a bien été ajouté dans la base
            try (Connection conn = databaseManager.getCon();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM historique_de_recherche WHERE Id_utilisateur = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "L'historique de recherche devrait exister dans la base de données.");
                    assertEquals(userId, rs.getInt("Id_utilisateur"), "L'ID de l'utilisateur dans l'historique doit correspondre.");
                    assertNull(rs.getString("Ville1"), "Ville1 doit être initialement NULL.");
                    assertNull(rs.getString("Ville2"), "Ville2 doit être initialement NULL.");
                    assertNull(rs.getString("Ville3"), "Ville3 doit être initialement NULL.");
                    assertNull(rs.getString("Ville4"), "Ville4 doit être initialement NULL.");
                    assertNull(rs.getString("Ville5"), "Ville5 doit être initialement NULL.");
                }
            }

            System.out.println("Test de l'ajout de l'historique de recherche réussi.");
        } catch (SQLException e) {
            e.printStackTrace(); // Affiche plus d'informations en cas d'erreur
            fail("Une exception SQL a été levée : " + e.getMessage());
        }
    }


    @Test
    public void testIsHistoryExist() {
        try {
            int userId = 112; // ID de test
            boolean exists = databaseManager.isHistoryExist(userId);
            assertTrue(exists, "L'historique de recherche devrait exister pour l'utilisateur.");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testUpdateSearchHistory() {
        try {
            int userId = 112; // ID de test
            String city = "Paris"; // Ville à ajouter
            boolean result = databaseManager.updateSearchHistory(userId, city);
            assertTrue(result, "L'historique de recherche devrait être mis à jour avec la ville.");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testClearSearchHistory() {
        try {
            int userId = 112; // ID de test

            // Vérifier que l'historique de recherche existe avant la suppression
            List<String> citiesBeforeClear = databaseManager.getLastFiveCities(userId);
            assertFalse(citiesBeforeClear.isEmpty(), "L'historique de recherche ne devrait pas être vide avant la suppression.");

            // Supprimer l'historique de recherche
            boolean result = databaseManager.clearSearchHistory(userId);
            assertTrue(result, "L'historique de recherche devrait être supprimé avec succès.");

            // Vérifier que l'historique de recherche est vide après la suppression
            List<String> citiesAfterClear = databaseManager.getLastFiveCities(userId);
            assertTrue(citiesAfterClear.isEmpty(), "L'historique de recherche devrait être vide après la suppression.");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testAddFiveCitiesAndRetrieveHistory() {
        try {
            int userId = 112; // ID de l'utilisateur de test

            // Créer une instance de SearchHistory pour gérer l'historique
            SearchHistory searchHistory = new SearchHistory(93,databaseManager); // Assurez-vous que la classe SearchHistory utilise DatabaseManager

            // Ajouter 5 villes à l'historique de recherche de l'utilisateur
            String[] cities = {"Paris", "London", "New York", "Tokyo", "Berlin"};

            for (String city : cities) {
                // Ajouter chaque ville à l'historique de l'utilisateur
                searchHistory.addCityToHistory(userId, city);
            }

            // Récupérer l'historique des 5 dernières villes
            List<String> citiesFromDb = databaseManager.getLastFiveCities(userId);

            // Vérifier que l'historique contient bien 5 villes
            assertEquals(5, citiesFromDb.size(), "L'historique de recherche devrait contenir 5 villes.");

            // Vérifier que chaque ville a bien été ajoutée
            for (String city : cities) {
                assertTrue(citiesFromDb.contains(city), "L'historique devrait contenir la ville : " + city);
            }

            System.out.println("Test de l'ajout de 5 villes et récupération réussi.");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("Une exception d'argument illégal a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testInsertWeatherDataWithCityValidation() {
        try {
            // Initialisation de la ville et des données météo
            String cityName = "Marrakech";
            double temperature = 22;
            double rainChance = 30.0;

            // Étape 1 : Vérification si la ville est valide via l'API
            boolean isValidCity = databaseManager.isCityValid(cityName);
            assertTrue(isValidCity, "La ville devrait être valide.");

            // Si la ville est valide, on insère les données météo
            if (isValidCity) {
                databaseManager.insertWeatherData(cityName, temperature, rainChance);

                // Vérification que les données sont insérées dans la base de données
                String query = "SELECT * FROM datacity WHERE CityName = ?";
                try (Connection conn = databaseManager.getCon();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, cityName);
                    ResultSet rs = stmt.executeQuery();

                    assertTrue(rs.next(), "Les données météo pour la ville devraient être présentes.");
                    assertEquals(cityName, rs.getString("CityName"), "Le nom de la ville ne correspond pas.");
                    assertEquals(temperature, rs.getDouble("Temperature"), 0.1, "La température ne correspond pas.");
                    assertEquals(rainChance, rs.getDouble("RainChance"), 0.1, "La probabilité de pluie ne correspond pas.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testInsertWeatherDataWithInvalidCity() {
        try {
            // Initialisation d'une ville invalide et des données météo
            String cityName = "NowhereCity";  // Ville qui n'existe pas
            double temperature = 22.5;
            double rainChance = 50.0;

            // Étape 1 : Vérification si la ville est valide via l'API
            boolean isValidCity = databaseManager.isCityValid(cityName);
            assertFalse(isValidCity, "La ville ne devrait pas être valide.");

            // Si la ville n'est pas valide, les données météo ne doivent pas être insérées
            if (!isValidCity) {
                System.out.println("La ville " + cityName + " n'est pas valide. Aucune donnée météo insérée.");
            } else {
                databaseManager.insertWeatherData(cityName, temperature, rainChance);

                // Vérification que les données sont insérées dans la base de données
                String query = "SELECT * FROM datacity WHERE CityName = ?";
                try (Connection conn = databaseManager.getCon();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, cityName);
                    ResultSet rs = stmt.executeQuery();

                    assertFalse(rs.next(), "Les données météo ne devraient pas être insérées pour une ville invalide.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }
    @Test
    public void testGetRainChanceForToday_ValidCity() {
        // Créer une instance de DatabaseManager
        DBConnexion dbConnexion = new DBConnexion();
        DatabaseManager databaseManager = new DatabaseManager(dbConnexion);

        // Ville valide pour tester
        String cityName = "Vigo";

        // Appeler la méthode pour obtenir la probabilité de pluie
        double rainChance = databaseManager.getRainChanceForToday(cityName);

        // Vérifier que la probabilité de pluie retournée est valide (elle devrait être > 0 ou -1 en cas d'erreur)
        assertTrue(rainChance >= 0, "La probabilité de pluie devrait être un nombre valide.");
        System.out.println("Probabilité de pluie pour " + cityName + ": " + rainChance);
    }

    @Test
    public void testGetRainChanceForToday_InvalidCity() {
        // Créer une instance de DatabaseManager
        DBConnexion dbConnexion = new DBConnexion();
        DatabaseManager databaseManager = new DatabaseManager(dbConnexion);

        // Ville invalide pour tester
        String cityName = "InvalidCityName";

        // Appeler la méthode pour obtenir la probabilité de pluie
        double rainChance = databaseManager.getRainChanceForToday(cityName);

        // Vérifier que la probabilité de pluie retournée est -1 (valeur par défaut en cas d'erreur)
        assertEquals(-1, rainChance, "La probabilité de pluie pour une ville invalide devrait être -1.");
        System.out.println("Probabilité de pluie pour " + cityName + ": " + rainChance);
    }
    @AfterAll
    void closeDatabaseConnection() {
        try {
            dbConnexion.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testGetAverageTemperatureForDay_ValidCity() {
        // Créer une instance de DatabaseManager
        DBConnexion dbConnexion = new DBConnexion();
        DatabaseManager databaseManager = new DatabaseManager(dbConnexion);

        // Ville valide pour tester
        String cityName = "Safi";

        // Appeler la méthode pour obtenir la température moyenne
        double avgTemperature = databaseManager.getAverageTemperatureForDay(cityName);

        // Vérifier que la température moyenne retournée est valide (elle devrait être > 0 ou -1 en cas d'erreur)
        assertTrue(avgTemperature >= -1, "La température moyenne devrait être un nombre valide.");
        System.out.println("Température moyenne pour " + cityName + ": " + avgTemperature);
    }

    @Test
    public void testGetAverageTemperatureForDay_InvalidCity() {
        // Créer une instance de DatabaseManager
        DBConnexion dbConnexion = new DBConnexion();
        DatabaseManager databaseManager = new DatabaseManager(dbConnexion);

        // Ville invalide pour tester
        String cityName = "InvalidCityName";

        // Appeler la méthode pour obtenir la température moyenne
        double avgTemperature = databaseManager.getAverageTemperatureForDay(cityName);

        // Vérifier que la température moyenne retournée est -1 (valeur par défaut en cas d'erreur)
        assertEquals(-1, avgTemperature, "La température pour une ville invalide devrait être -1.");
        System.out.println("Température pour " + cityName + ": " + avgTemperature);
    }
    @Test
    void testGetUserEmail() {
        int userId = 112; // ID de l'utilisateur qui doit avoir l'email "ben10aabir@gmail.com"
        String expectedEmail = "john.oe@example.com";

        // Créer une instance de DatabaseManager
        DBConnexion dbConnexion = new DBConnexion();
        DatabaseManager databaseManager = new DatabaseManager(dbConnexion);

        String actualEmail = databaseManager.getUserEmail(userId);

        // Afficher un message pour vérifier le résultat
        System.out.println("Expected email: " + expectedEmail);
        System.out.println("Actual email: " + actualEmail);

        assertEquals(expectedEmail, actualEmail, "L'email récupéré doit correspondre à l'email attendu.");
    }

    @Test
    public void testCreateUserWithSearchHistoryAndPreferences_Success() throws SQLException {
        // Arrange : Créer un utilisateur et les données nécessaires
        User user = new User(116, "Dj", "Joh", "joh.doe@example.com", "paword123", 0);
        String villeDefaut = "Paris";
        String V1 = "Londres", V2 = "Berlin", V3 = "Rome", V4 = "Tokyo", V5 = "Madrid";
        String unite = "°C";

        // Étape 1 : Appel de la méthode createUserWithSearchHistoryAndPreferences
        boolean userCreated = databaseManager.createUserWithSearchHistoryAndPreferences(user, villeDefaut, V1, V2, V3, V4, V5, unite);

        // Vérifier que l'utilisateur, son historique de recherche et ses préférences sont créés avec succès
        assertTrue(userCreated, "L'utilisateur, son historique de recherche et ses préférences devraient être créés avec succès.");

        // Étape 2 : Validation dans la base de données pour l'utilisateur
        String userQuery = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(userQuery)) {
            stmt.setString(1, user.getEmail());

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "L'utilisateur doit exister dans la base de données.");
                assertEquals(user.getEmail(), rs.getString("email"));
                assertEquals(user.getPrenom(), rs.getString("Prenom"));
                assertEquals(user.getNom(), rs.getString("Nom"));
            }
        }

        // Étape 3 : Validation de l'historique de recherche
        String historyQuery = "SELECT * FROM historique_de_recherche WHERE Id_utilisateur = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(historyQuery)) {
            stmt.setInt(1, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "L'historique de recherche doit être ajouté pour l'utilisateur.");
            }
        }

        // Étape 4 : Validation des préférences par défaut
        String preferencesQuery = "SELECT * FROM preferences WHERE Id_utilisateur = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(preferencesQuery)) {
            stmt.setInt(1, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Les préférences par défaut doivent être insérées pour l'utilisateur.");
                assertEquals(villeDefaut, rs.getString("Ville_par_defaut"));
                assertEquals(unite, rs.getString("Unite"));
            }
        }
    }
    @Test
    public void testCreateDefaultPreferences_Success() throws SQLException {
        // Arrange : Données de test
        int userId = 116;  // Assurez-vous que cet utilisateur existe déjà dans votre base de données ou créez-le au préalable
        String villeDefaut = "Paris";
        String V1 = "Londres", V2 = "Berlin", V3 = "Rome", V4 = "Tokyo", V5 = "Madrid";
        String unite = "°C";

        // Étape 1 : Appel de la méthode createDefaultPreferences
        boolean preferencesCreated = databaseManager.createDefaultPreferences(userId, villeDefaut, V1, V2, V3, V4, V5, unite);

        // Vérifier que la méthode renvoie true si l'insertion réussit
        assertTrue(preferencesCreated, "Les préférences par défaut devraient être créées avec succès.");

        // Étape 2 : Validation dans la base de données
        String query = "SELECT * FROM preferences WHERE Id_utilisateur = ? AND Ville_par_defaut = ? AND Unite = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, villeDefaut);
            stmt.setString(3, unite);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Les préférences par défaut doivent être insérées dans la base de données.");
                assertEquals(userId, rs.getInt("Id_utilisateur"));
                assertEquals(villeDefaut, rs.getString("Ville_par_defaut"));
                assertEquals(unite, rs.getString("Unite"));
            }
        }
    }



}
