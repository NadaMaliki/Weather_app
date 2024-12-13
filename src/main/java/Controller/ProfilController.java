package Controller;

import DAO.DatabaseManager;
import DAO.UserPreferencesManager;
import Model.User;
import Model.UserPreferences;

import java.util.List;

public class ProfilController {

    private final DatabaseManager databaseManager;
    private final UserPreferencesManager preferencesManager;
    private final UserPreferences preferences;

    public ProfilController(DatabaseManager databaseManager, UserPreferencesManager preferencesManager, UserPreferences preferences) {
        this.databaseManager = databaseManager;
        this.preferencesManager = preferencesManager;
        this.preferences = preferences;
    }

    // Récupérer les informations de l'utilisateur connecté
    public User getCurrentUser() {
        return Session.getCurrentUser();
    }

    // Récupérer la ville par défaut de l'utilisateur
    public String getUserDefaultCity(int userId) throws Exception {
        return preferencesManager.getDefaultCity(userId);
    }

    // Récupérer la liste des villes préférées de l'utilisateur
    public List<String> getFavoriteCities(int userId) throws Exception {
        return preferencesManager.getFavoriteCities(userId);
    }

    // Récupérer l'unité de l'utilisateur
    public String getUserUnit(int userId) throws Exception {
        return preferencesManager.getUnit(userId);
    }

    // Récupérer la préférence d'alerte de l'utilisateur
    public boolean getUserAlertPreference(int userId) throws Exception {
        return preferencesManager.getAlerte(userId);
    }

    // Mettre à jour une ville préférée
    public void updateFavoriteCity(int userId, int index, String updatedCity) throws Exception {
    	preferencesManager.updateFavoriteCity(userId,  index,updatedCity);
    }

    // Supprimer une ville préférée
    public void removeFavoriteCity(int userId, int index) throws Exception {
        preferencesManager.removeFavoriteCity(userId, index);
    }

    // Modifier la ville par défaut
    public void updateDefaultCity(int userId, String newCity) throws Exception {
        if (preferences.editDefaultCity(userId, newCity)) {
            System.out.println("Ville par défaut mise à jour avec succès.");
        } else {
            System.err.println("Échec de la mise à jour de la ville par défaut.");
        }
    }
    public boolean isCityValid(String city) {
    	
		return databaseManager.isCityValid(city);
	}
}
