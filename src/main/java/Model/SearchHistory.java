package Model;



import Dao.DatabaseManager;

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

    public void addCityToHistory(int userId, String newCity) throws SQLException {
        if (newCity == null || newCity.trim().isEmpty()) {
            throw new IllegalArgumentException("La ville ne peut pas être vide ou nulle.");
        }

        // Si l'historique n'existe pas, on l'ajoute
        if (!databaseManager.isHistoryExist(userId)) {
            boolean historyCreated = databaseManager.addSearchHistory(userId);
            if (!historyCreated) {
                throw new SQLException("Échec de la création de l'historique de recherche pour l'utilisateur ID " + userId);
            }
        }

        // Mise à jour de l'historique de recherche avec la nouvelle ville
        boolean isUpdated = databaseManager.updateSearchHistory(userId, newCity.trim());
        if (isUpdated) {
            System.out.println("Ville ajoutée à l'historique : " + newCity);
        } else {
            throw new SQLException("Échec de la mise à jour de l'historique pour l'utilisateur ID " + userId);
        }
    }




//Récupère les 5 dernières villes recherchées par l'utilisateur.

    public List<String> getHistory() throws SQLException {
        try {
            List<String> history = databaseManager.getLastFiveCities(userId);
            return history.stream()
                    .filter(city -> city != null && !city.trim().isEmpty()) // Filtrage des valeurs invalides
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique : " + e.getMessage());
            return new ArrayList<>(); // Retourne une liste vide en cas d'erreur
        }
    }



    //Supprime tout l'historique de recherche de l'utilisateur.
    public void clearHistory() {
        try {
            databaseManager.clearSearchHistory(userId);
            System.out.println("Historique des recherches supprimé avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'historique : " + e.getMessage());
        }
    }
}

