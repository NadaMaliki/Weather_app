package Model;

import DAO.DatabaseManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SearchHistory {
    private final int userId; // Identifiant de l'utilisateur
    private final DatabaseManager databaseManager; // Gestionnaire de base de données


    public SearchHistory(int userId, DatabaseManager databaseManager) {
        this.userId = userId;
        this.databaseManager = databaseManager;
    }


    public void addCityToHistory(String newCity) throws SQLException {
        databaseManager.updateSearchHistory(userId, newCity); // Mise à jour logique dans la DB
        System.out.println("Ville ajoutée à l'historique : " + newCity);
    }


    public List<String> getHistory()throws SQLException {
        try {
            List<String> history = databaseManager.getLastFiveCities(userId);
            return history.stream()
                    .filter(city -> city != null && !city.isEmpty()) // Filtrage des valeurs nulles ou vides
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique : " + e.getMessage());
            return new ArrayList<>(); // Retourne une liste vide en cas d'erreur
        }
    }



    public void clearHistory() {
        try {
            databaseManager.clearSearchHistory(userId);
            System.out.println("Historique des recherches supprimé avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'historique : " + e.getMessage());
        }
    }
}
