package project.weather_app;



import Dao.DatabaseManager;
import Dao.DBConnexion;
import Model.SearchHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchHistoryTest {

    private DatabaseManager databaseManager;
    private SearchHistory searchHistory;

    @BeforeEach
    void setUp() throws SQLException {
        // Initialisation de la connexion à la base de données et du DatabaseManager
        DBConnexion dbConnexion = new DBConnexion(); // Assurez-vous que la configuration est correcte
        databaseManager = new DatabaseManager(dbConnexion);

        // Initialiser SearchHistory pour un utilisateur spécifique
        searchHistory = new SearchHistory(59, databaseManager);
    }



    @Test
    public void testAddCityToHistory() {
        try {
            int userId = 101; // ID de test
            String city = "Paris"; // Ville à ajouter
            SearchHistory searchHistory = new SearchHistory(userId, databaseManager);
            searchHistory.addCityToHistory(userId, city);  // Appel de la méthode
            List<String> history = searchHistory.getHistory();
            assertTrue(history.contains(city), "L'historique devrait contenir la ville ajoutée.");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }


    @Test
    public void testGetHistory() {
        try {
            int userId = 101; // ID de test
            SearchHistory searchHistory = new SearchHistory(userId, databaseManager);
            List<String> history = searchHistory.getHistory();
            assertNotNull(history, "L'historique ne doit pas être nul.");
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Une exception SQL a été lancée : " + e.getMessage());
        }
    }


    @Test
    public void testClearSearchHistory() {
        try {
            int userId = 101; // ID de test

            // Récupérer les villes de l'historique avant de tester la suppression
            List<String> citiesBeforeClear = databaseManager.getLastFiveCities(userId);

            // Si l'historique est vide, ajoutons une ville de test
            if (citiesBeforeClear.isEmpty()) {
                // Ajouter une ville si l'historique est vide
                String city = "Paris";
                boolean updated = databaseManager.updateSearchHistory(userId, city);
                assertTrue(updated, "La ville devrait être ajoutée à l'historique.");
                citiesBeforeClear = databaseManager.getLastFiveCities(userId);
            }

            // Vérifier que l'historique n'est pas vide avant la suppression
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

}
