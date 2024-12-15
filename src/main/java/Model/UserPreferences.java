package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Dao.UserPreferencesManager;
import Dao.DBConnexion;

public class UserPreferences {
    private int id;
    private int userId;
    private String defaultcity;
    private List<String> favoriteCities = new ArrayList<>();
    private String unit;  // Celsius, Kelvin, Fahrenheit
    private boolean alerte;
    private UserPreferencesManager dbManager;


    //constructeur avec la dbManager only
    public UserPreferences(UserPreferencesManager dbManager) {
        this.dbManager = dbManager;
    }

    // Constructeur de la classe UserPreferences
    public UserPreferences(int userId, DBConnexion dbConnexion) throws SQLException {
        this.userId = userId;
        this.dbManager = new UserPreferencesManager(dbConnexion); // Fournir l'instance de DBConnexion
        loadPreferencesFromDatabase();
    }

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDefaultcity() {
        return defaultcity;
    }

    public void setDefaultcity(String defaultcity) {
        this.defaultcity = defaultcity;
    }

    public List<String> getFavoriteCities() {
        return favoriteCities;
    }

    public void setFavoriteCities(List<String> favoriteCities) {
        this.favoriteCities = favoriteCities;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isAlerte() {
        return alerte;
    }

    public void setAlerte(boolean alerte) {
        this.alerte = alerte;
    }

    public UserPreferencesManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(UserPreferencesManager dbManager) {
        this.dbManager = dbManager;
    }

    // Charger les préférences depuis la base de données
    private void loadPreferencesFromDatabase() throws SQLException {
        String query = "SELECT Ville1, Ville2, Ville3, Ville4, Ville5, Unite FROM preferences WHERE Id_utilisateur = ?";
        try (Connection conn = dbManager.getCon();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId); // Utiliser l'ID de l'utilisateur
            ResultSet rs = pstmt.executeQuery();

            favoriteCities.clear(); // Réinitialiser la liste des villes favorites
            if (rs.next()) {
                for (int i = 1; i <= 5; i++) {
                    String city = rs.getString("Ville" + i);
                    if (city != null) {
                        favoriteCities.add(city);
                    }
                }
                unit = rs.getString("Unite") != null ? rs.getString("Unite") : "Celsius";  // Valeur par défaut si l'unité est nulle
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des préférences : " + e.getMessage());
            throw new SQLException("Erreur lors du chargement des préférences", e);
        }
    }


    // Méthode pour obtenir la ville par défaut
    public String getDefaultCity(int userId) {
        return dbManager.getDefaultCity(userId);
    }

    // Méthode pour modifier la ville par défaut
    public boolean editDefaultCity(int userId, String newCity) {
        // Vérification de la validité de la ville
        if (newCity == null || newCity.isEmpty()) {
            throw new IllegalArgumentException("La ville par defaut ne peut pas être vide.");
        }
        return dbManager.editDefaultCity(userId, newCity);
    }

    // Méthode pour obtenir l'unite
    public String getUnit(int userId) {
        return dbManager.getUnit(userId);
    }

    // Méthode pour modifier l'unite
    public boolean editUnit(int userId, String unit) {


        return dbManager.updateUnit(userId, unit);
    }

    // Méthode pour verifier si l'alerte est active
    public boolean isAlerteActive() {
        return dbManager.getAlerte(this.userId);
    }

    // Méthode pour activer l'alerte
    public boolean toggleAlerte(boolean activate) {
        return dbManager.setAlerte(this.userId, activate);
    }

    // Méthode pour obtenir les fav cities
    public List<String> getFavoriteCities(int userId) {
        return dbManager.getFavoriteCities(userId);
    }

    // Méthode pour ajouter une fav city
    public boolean addFavoriteCity(int id, String city) {
        return dbManager.addFavoriteCity(this.userId, city);
    }

    // Méthode pour modifier une fav city
    public boolean editFavoriteCity(int index, String city) {
        return dbManager.updateFavoriteCity(this.userId, index, city);
    }
    

    // Méthode pour modifier une fav city
    public boolean removeFavoriteCity(int index) {
        return dbManager.removeFavoriteCity(this.userId, index);
    }
}
