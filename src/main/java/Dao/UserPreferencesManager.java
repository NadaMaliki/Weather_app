package Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserPreferencesManager {
    private final DBConnexion dbConnexion;


    public UserPreferencesManager(DBConnexion dbConnexion) {
        this.dbConnexion = dbConnexion;
    }

    // Méthode publique pour obtenir la connexion
    public Connection getCon() throws SQLException {
        return dbConnexion.getCon(); // Appel direct à la méthode dans DBConnexion
    }


    // Méthode publique pour obtenir la connexion avecla base du teste
    public Connection getContest(String URL, String User, String Password) throws SQLException {
        return dbConnexion.getContest(URL, User, Password); // Appel direct à la méthode dans DBConnexion
    }



    // les methodes d'user preferences class
    // Méthode pour récupérer la ville par défaut d'un utilisateur
    public String getDefaultCity(int userId) {
        // Exemple de requête SQL
        String query = "SELECT Ville_par_defaut FROM preferences WHERE Id_utilisateur = ?";
        try (Connection conn = getCon(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Ville_par_defaut");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Casablanca"; // une valeur par défaut
    }



    public boolean editDefaultCity(int userId, String newCity) {
        String query = "UPDATE preferences SET Ville_par_defaut = ? WHERE Id_utilisateur = ?";
        try (PreparedStatement stmt = getCon().prepareStatement(query)) {
            stmt.setString(1, newCity);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Méthode pour récupérer l'unite' d'un utilisateur
    public String getUnit(int userId) {
        String unit = null;
        String query = "SELECT unite FROM preferences WHERE Id_utilisateur = ?";

        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    unit = rs.getString("unite");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unit;
    }

    // Méthode pour mettre à jour l'unite d'un utilisateur
    public boolean updateUnit(int userId, String unit) {
        String query = "UPDATE preferences SET unite = ? WHERE Id_utilisateur = ?";

        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setString(1, unit);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Méthode pour convertir les températures en fonction de l'unité
    public double convertTemperature(double temperatureInCelsius, String unit) {
        switch (unit) {
            case "°K":
                return temperatureInCelsius + 273.15;
            case "°F":
                return (temperatureInCelsius * 9 / 5) + 32;
            default:
                return temperatureInCelsius; // Celsius par défaut
        }
    }
    // Méthode pour récupérer l'alerte
    public boolean getAlerte(int userId) {
        String query = "SELECT alerte FROM utilisateur WHERE Id = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("alerte");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Retourne false par défaut si l'utilisateur n'existe pas ou en cas d'erreur
    }

    // Méthode pour mettre à jour l'alerte
    public boolean setAlerte(int userId, boolean alerte) {
        String query = "UPDATE utilisateur SET alerte = ? WHERE Id = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setBoolean(1, alerte);
            stmt.setInt(2, userId);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode pour récupérer les villes preferees
    public List<String> getFavoriteCities(int userId) {
        List<String> favoriteCities = new ArrayList<>();
        String query = "SELECT Ville1, Ville2, Ville3, Ville4, Ville5 FROM preferences WHERE Id_utilisateur = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (int i = 1; i <= 5; i++) {
                        String city = rs.getString("Ville" + i);
                        if (city != null && !city.isEmpty()) {
                            favoriteCities.add(city);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteCities;
    }

    // Méthode pour ajouter une ville preferee
    public boolean addFavoriteCity(int userId, String newCity) {
        List<String> cities = getFavoriteCities(userId);
        if (cities.size() >= 5) {
            return false; // Déjà 5 villes favorites
        }
        String query = "UPDATE preferences SET Ville" + (cities.size() + 1) + " = ? WHERE Id_utilisateur = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setString(1, newCity);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateFavoriteCity(int userId, int index, String updatedCity) {
        if (index < 1 || index > 5) {
            return false; // Index hors limites
        }
        String query = "UPDATE preferences SET Ville" + index + " = ? WHERE Id_utilisateur = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setString(1, updatedCity);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode pour supprimer une ville preferee
    public boolean removeFavoriteCity(int userId, int index) {
        if (index < 1 || index > 5) {
            return false; // Index hors limites
        }
        String query = "SELECT Ville1, Ville2, Ville3, Ville4, Ville5 FROM preferences WHERE Id_utilisateur = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Réorganiser les villes après suppression
                    List<String> cities = new ArrayList<>();
                    for (int i = 1; i <= 5; i++) {
                        if (i != index) {
                            cities.add(rs.getString("Ville" + i));
                        }
                    }
                    while (cities.size() < 5) {
                        cities.add(null); // Ajouter des valeurs null pour les colonnes vides
                    }
                    // Mettre à jour les villes
                    String updateQuery = "UPDATE preferences SET Ville1 = ?, Ville2 = ?, Ville3 = ?, Ville4 = ?, Ville5 = ? WHERE Id_utilisateur = ?";
                    try (PreparedStatement updateStmt = dbConnexion.getCon().prepareStatement(updateQuery)) {
                        for (int i = 0; i < 5; i++) {
                            updateStmt.setString(i + 1, cities.get(i));
                        }
                        updateStmt.setInt(6, userId);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }





}
