package Dao;



import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import Model.User;

import Services.Api.CityValidationApi;
import Services.Api.WeatherApi;
import java.util.concurrent.Callable;
import com.fasterxml.jackson.databind.JsonNode;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


public class DatabaseManager {
    private final DBConnexion dbConnexion;
    private final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private final CityValidationApi cityValidationApi = new CityValidationApi();
    private final WeatherApi weatherApi = new WeatherApi();


    // Constructeur pour injecter DBConnexion
    public DatabaseManager(DBConnexion dbConnexion)
    {
        this.dbConnexion = dbConnexion;
    }
    // Méthode publique pour obtenir la connexion
    public Connection getCon() throws SQLException {
        return DBConnexion.getCon(); // Appel direct à la méthode dans DBConnexion
    }

    // Gestion des transactions avec Callable
    public boolean executeWithTransaction(Callable<Boolean> action) throws SQLException {
        try (Connection conn = DBConnexion.getCon()) {
            conn.setAutoCommit(false);
            try {
                boolean result = action.call();
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Transaction error: {0}", e.getMessage());
                throw new SQLException("Error during transaction", e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    public int addUser(User user) throws SQLException {
        int userId = -1; // Valeur par défaut pour indiquer un échec
        try (Connection conn = dbConnexion.getCon()) {
            conn.setAutoCommit(false); // Désactive l'auto-commit pour la gestion manuelle des transactions

            // Vérifie si l'email existe déjà dans la base de données
            String checkEmailSql = "SELECT COUNT(*) FROM utilisateur WHERE Email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql)) {
                checkStmt.setString(1, user.getEmail());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // L'email existe déjà, retourne -1 pour indiquer un échec
                        return -1;
                    }
                }
            }

            // Insère le nouvel utilisateur
            String insertSql = "INSERT INTO utilisateur (Nom, Prenom, Email, password, Alerte) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, user.getNom());
                insertStmt.setString(2, user.getPrenom());
                insertStmt.setString(3, user.getEmail());
                insertStmt.setString(4, hashPassword(user.getPassword())); // Hash du mot de passe avant insertion
                insertStmt.setInt(5, user.isAlerte());

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            userId = generatedKeys.getInt(1); // Récupère l'ID généré
                        }
                    }
                    conn.commit(); // Valide la transaction si l'insertion est réussie
                } else {
                    conn.rollback(); // Annule la transaction si aucune ligne n'a été insérée
                }
            } catch (SQLException e) {
                conn.rollback(); // Annule la transaction en cas d'erreur
                logger.log(Level.SEVERE, "Erreur lors de l'ajout de l'utilisateur : {0}", e.getMessage());
                throw e; // Relance l'exception après le rollback
            } finally {
                conn.setAutoCommit(true); // Réactive l'auto-commit
            }
        }
        return userId; // Retourne l'ID généré ou -1 si l'insertion a échoué
    }


    public User getUserById(int id) throws SQLException {
        User user = null;
        try (Connection conn = DBConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM utilisateur WHERE Id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User(
                        rs.getInt("Id"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Email"),
                        rs.getString("password"),
                        rs.getInt("Alerte")
                );
            }
        }
        return user;
    }


    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM utilisateur WHERE email = ?";
        try (Connection conn = DBConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Assurez-vous que la colonne Id_utilisateur existe dans le ResultSet
                    return new User(
                            rs.getInt("Id"),
                            rs.getString("Nom"),
                            rs.getString("Prenom"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("Alerte") // Ajustez selon votre table
                    );
                } else {
                    return null; // Utilisateur non trouvé
                }
            }
        }
    }
    // Mettre à jour un utilisateur
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE utilisateur SET Nom = ?, Prenom = ?, Email = ?, password = ?, Alerte = ? WHERE Id = ?";
        return executeWithTransaction(() -> {
            try (PreparedStatement stmt = DBConnexion.getCon().prepareStatement(query)) {
                stmt.setString(1, user.getNom());
                stmt.setString(2, user.getPrenom());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, hashPassword(user.getPassword()));
                stmt.setInt(5, user.isAlerte());
                stmt.setInt(6, user.getId());
                return stmt.executeUpdate() > 0;
            }
        });
    }
    // Méthode pour créer une ligne par défaut dans la table preferences
    public boolean createDefaultPreferences(int userId, String villeDefaut, String V1, String V2, String V3, String V4, String V5, String unite) throws SQLException {
        UserPreferencesManager userpr = new UserPreferencesManager(dbConnexion);

        List<String> villes = new ArrayList<String>();
        villes.add(V1);
        villes.add(V2);
        villes.add(V3);
        villes.add(V4);
        villes.add(V5);

        villes.removeIf(item -> item == null );

        for(int i=0 ; i<5 ; i++) {
            System.out.println(villes.get(i));
        }

        String query = "INSERT INTO preferences (Id_utilisateur, Ville_par_defaut, Unite) " +
                "VALUES (?, ?, ?)";



        try (Connection conn = getCon(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, villeDefaut);
            stmt.setString(3, unite);
            int rowsInserted = stmt.executeUpdate();

            for(int i=0 ; i<5 ; i++) {
                userpr.addFavoriteCity(userId, villes.get(i));
            }
            return rowsInserted > 0; // Retourne true si une ligne a été insérée
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Erreur lors de la création des préférences par défaut pour l'utilisateur ID: " + userId, e);
        }
    }

    public boolean createUserWithSearchHistoryAndPreferences(User newUser, String villeDefaut, String V1, String V2, String V3, String V4, String V5, String unite) throws SQLException {
        try (Connection conn = dbConnexion.getCon()) { // Utilisation de try-with-resources pour gérer la connexion
            conn.setAutoCommit(false); // Désactive l'auto-commit pour gérer la transaction manuellement

            // Étape 1 : Ajouter l'utilisateur dans la table utilisateur
            int userId = addUser(newUser);
            if (userId == -1) {
                // conn.rollback();
                return false; // Échec de l'ajout de l'utilisateur
            }

            // Étape 2 : Ajouter une entrée dans l'historique de recherche
            boolean historyAdded = addSearchHistory(userId);
            if (!historyAdded) {
                //conn.rollback(); // Annuler la transaction
                return false;
            }

            // Étape 3 : Ajouter une entrée par défaut dans la table préférences
            boolean preferencesAdded = createDefaultPreferences(userId, villeDefaut, V1, V2, V3, V4, V5, unite);
            if (!preferencesAdded) {
                //  conn.rollback(); // Annuler la transaction
                return false;
            }

            // Valider la transaction
            // conn.commit();
            return true;
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la création de l'utilisateur, de l'historique et des préférences", e);
        }
    }

    // Supprimer un utilisateur par ID
    public boolean deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM utilisateur WHERE Id = ?";
        return executeWithTransaction(() -> {
            try (PreparedStatement pstmt = dbConnexion.getCon().prepareStatement(query)) {
                pstmt.setInt(1, userId);
                return pstmt.executeUpdate() > 0;
            }
        });
    }
    // Authentifier un utilisateur
    public boolean isUserAuthenticated(String email, String password) {
        String query = "SELECT password FROM utilisateur WHERE Email = ?";
        password = password.trim();

        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Mot de passe haché récupéré de la base de données
                String storedHashedPassword = rs.getString("password");
                logger.log(Level.INFO, "Mot de passe récupéré pour l'utilisateur : {0}", email);

                if (storedHashedPassword != null) {
                    // Vérifie si le mot de passe utilise un format BCrypt compatible
                    if (storedHashedPassword.matches("^\\$2[aby]\\$.*")) {
                        logger.log(Level.INFO, "Format du mot de passe valide pour l'utilisateur : {0}", email);

                        // Vérification du mot de passe
                        boolean isPasswordCorrect = verifyPassword(password, storedHashedPassword);
                        if (isPasswordCorrect) {
                            logger.log(Level.INFO, "Authentification réussie pour l'utilisateur : {0}", email);
                            return true;
                        } else {
                            logger.log(Level.WARNING, "Mot de passe incorrect pour l'utilisateur : {0}", email);
                        }
                    } else {
                        logger.log(Level.WARNING, "Mot de passe dans un format incompatible pour l'utilisateur : {0}.", email);
                        handleRehashing(email, password, storedHashedPassword);
                    }
                } else {
                    logger.log(Level.WARNING, "Mot de passe non trouvé pour l'utilisateur : {0}", email);
                }
            } else {
                logger.log(Level.WARNING, "Aucun utilisateur trouvé avec l'email : {0}", email);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur SQL lors de l'authentification de l'utilisateur : {0}. Message : {1}",
                    new Object[]{email, e.getMessage()});
        }
        logger.log(Level.INFO, "L'authentification a échoué pour l'utilisateur : {0}", email);
        return false;
    }

    /**
     * Gère le re-hachage et la mise à jour du mot de passe dans la base de données.
     */
    private void handleRehashing(String email, String plainPassword, String oldHashedPassword) {
        try {
            if (verifyPassword(plainPassword, oldHashedPassword)) {
                String newHashedPassword = hashPassword(plainPassword);
                updatePasswordInDatabase(email, newHashedPassword);
                logger.log(Level.INFO, "Mot de passe re-haché et mis à jour pour l'utilisateur : {0}", email);
            } else {
                logger.log(Level.WARNING, "Échec du re-hachage : mot de passe fourni incorrect pour l'utilisateur : {0}", email);
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Erreur lors du re-hachage du mot de passe pour l'utilisateur : {0}. Message : {1}",
                    new Object[]{email, e.getMessage()});
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Met à jour le mot de passe dans la base de données.
     */
    private void updatePasswordInDatabase(String email, String newHashedPassword) throws SQLException {
        String updateQuery = "UPDATE utilisateur SET password = ? WHERE Email = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, email);
            stmt.executeUpdate();
            logger.log(Level.INFO, "Mot de passe mis à jour dans la base de données pour l'utilisateur : {0}", email);
        }
    }

    // Assurez-vous que la méthode hashPassword utilise BCrypt pour le hachage
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // La méthode verifyPassword utilise BCrypt pour comparer les mots de passe
    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }


    // Méthodes pour la gestion de l'historique des recherches
    public List<String> getLastFiveCities(int userId) throws SQLException {
        String query = "SELECT Ville1, Ville2, Ville3, Ville4, Ville5 FROM historique_de_recherche WHERE Id_utilisateur = ?";
        List<String> cities = new ArrayList<>();

        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Parcours des colonnes Ville1 à Ville5
                for (int i = 1; i <= 5; i++) {
                    String city = rs.getString("Ville" + i);
                    if (city != null && !city.trim().isEmpty()) {
                        cities.add(city);  // Ajout des villes valides
                    }
                }
            }
        }

        System.out.println("Historique récupéré pour l'utilisateur ID " + userId + ": " + cities);  // Log pour débogage
        return cities;  // Liste des villes valides
    }


    public boolean updateSearchHistory(int userId, String newCity) throws SQLException {
        String query = "SELECT * FROM historique_de_recherche WHERE Id_utilisateur = ?";
        Connection conn = null;  // Déclare la connexion en dehors du try-catch

        try {
            conn = dbConnexion.getCon();  // Récupère la connexion à la base de données
            conn.setAutoCommit(false);  // Désactive l'auto-commit pour gérer la transaction manuellement

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Si les villes sont toutes NULL, insérer directement dans Ville1
                    String updateQuery = "UPDATE historique_de_recherche SET " +
                            "Ville5 = Ville4, Ville4 = Ville3, Ville3 = Ville2, Ville2 = Ville1, Ville1 = ? " +
                            "WHERE Id_utilisateur = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, newCity);
                        updateStmt.setInt(2, userId);
                        int rowsUpdated = updateStmt.executeUpdate();
                        conn.commit();  // Valider la transaction
                        return rowsUpdated > 0;
                    }
                } else {
                    conn.rollback();  // Annule la transaction si l'historique n'existe pas
                    return false;
                }
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();  // Annule la transaction en cas d'erreur
                }
                throw e;  // Relance l'exception pour informer l'appelant de l'erreur
            }
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);  // Réactive l'auto-commit
            }
        }
    }

    // Méthode pour supprimer tout l'historique des recherches d'un utilisateur
    public boolean clearSearchHistory(int userId) throws SQLException {
        String query = "UPDATE historique_de_recherche SET Ville1 = NULL, Ville2 = NULL, Ville3 = NULL, Ville4 = NULL, Ville5 = NULL WHERE Id_utilisateur = ?";
        Connection conn = null;  // Déclare la connexion en dehors du try-catch

        try {
            conn = dbConnexion.getCon();  // Récupère la connexion à la base de données
            conn.setAutoCommit(false);  // Désactive l'auto-commit pour gérer la transaction manuellement

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                int rowsUpdated = pstmt.executeUpdate();
                conn.commit();  // Valide la transaction
                return rowsUpdated > 0;
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();  // Annule la transaction en cas d'erreur
                }
                throw e;  // Relance l'exception pour informer l'appelant de l'erreur
            }
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);  // Réactive l'auto-commit
            }
        }
    }

    public boolean addSearchHistory(int userId) throws SQLException {
        String query = "INSERT INTO historique_de_recherche (Id_utilisateur, Ville1, Ville2, Ville3, Ville4, Ville5) VALUES (?, NULL, NULL, NULL, NULL, NULL)";
        Connection conn = null;

        try {
            conn = dbConnexion.getCon();  // Récupère la connexion à la base de données
            conn.setAutoCommit(false);  // Désactive l'auto-commit

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);  // Associe l'ID de l'utilisateur à la requête
                System.out.println("Ajout de l'historique pour l'utilisateur avec ID : " + userId); // Debug

                int rowsInserted = pstmt.executeUpdate();  // Exécute la requête d'insertion
                System.out.println("Nombre de lignes insérées : " + rowsInserted); // Debug

                if (rowsInserted > 0) {
                    conn.commit();  // Si l'insertion réussit, valide la transaction
                    return true;
                } else {
                    conn.rollback();  // Si l'insertion échoue, annule la transaction
                    return false;
                }
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();  // Annule la transaction en cas d'erreur
                }
                throw e;  // Relance l'exception pour informer l'appelant de l'erreur
            }
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);  // Réactive l'auto-commit
            }
        }
    }


    public boolean isHistoryExist(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM historique_de_recherche WHERE Id_utilisateur = ?";
        try (Connection conn = dbConnexion.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    // Valider si une ville est valide avant de chercher les données météo ou de stocker des informations dans la base de données
    public boolean isCityValid(String cityName) {
        return cityValidationApi.isCityValid(cityName);
    }

    // Insérer des données météo pour une ville donnée
    public void insertWeatherData(String cityName, double temperature, double rainChance) throws SQLException {
        if (!isCityValid(cityName)) {
            logger.log(Level.WARNING, "Ville invalide : {0}. Données non insérées.", cityName);
            return;
        }
        String query = "INSERT INTO datacity (CityName, Temperature, RainChance) VALUES (?, ?, ?)";
        executeWithTransaction(() -> {
            try (PreparedStatement pstmt = dbConnexion.getCon().prepareStatement(query)) {
                pstmt.setString(1, cityName);
                pstmt.setDouble(2, temperature);
                pstmt.setDouble(3, rainChance);
                return pstmt.executeUpdate() > 0;
            }
        });
    }
    // Méthode pour obtenir la probabilité de pluie pour le jour en cours
    public double getRainChanceForToday(String cityName) {
        JsonNode weatherData = weatherApi.getCurrentWeatherByCity(cityName);
        if (weatherData != null && weatherData.has("current") && weatherData.get("current").has("precip_mm")) {
            return weatherData.get("current").get("precip_mm").asDouble();
        }
        logger.log(Level.WARNING, "Données météo indisponibles pour la ville : {0}", cityName);
        return -1; // Valeur par défaut en cas d'erreur
    }


    // Méthode pour obtenir la température moyenne de la journée
    public double getAverageTemperatureForDay(String cityName) {
        JsonNode weatherData = weatherApi.getCurrentWeatherByCity(cityName);
        if (weatherData != null && weatherData.has("current")) {
            return weatherData.get("current").get("temp_c").asDouble();
        }
        return -1; // Valeur par défaut si les données sont indisponibles
    }

    // Récupérer l'email d'un utilisateur
    public String getUserEmail(int userId) {
        String query = "SELECT Email FROM utilisateur WHERE Id = ?";
        try (PreparedStatement stmt = dbConnexion.getCon().prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Email");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération de l'email : {0}", e.getMessage());
        }
        return null;
    }

    // Interface pour les transactions
    @FunctionalInterface
    private interface TransactionCallback {
        boolean execute() throws SQLException;
    }

}