package Controller;


import DAO.DatabaseManager;
import DAO.DBConnexion;
import DAO.UserPreferencesManager;
import Model.User;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpController {

    private final DatabaseManager databaseManager;
    private final UserPreferencesManager preferencesManager; 
    private static final Logger LOGGER = Logger.getLogger(SignUpController.class.getName());


    public SignUpController() {
        DBConnexion dbConnexion = new DBConnexion();
        this.databaseManager = new DatabaseManager(dbConnexion);
        this.preferencesManager = new UserPreferencesManager(dbConnexion);
    }

    /**
     * Méthode pour inscrire un nouvel utilisateur
     *
     * @param nom      Le nom de l'utilisateur
     * @param prenom   Le prénom de l'utilisateur
     * @param email    L'email de l'utilisateur
     * @param password Le mot de passe de l'utilisateur
     * @param alerte   Mode d'alerte (0 = aucun, 1 = email, 2 = notification)
     * @return True si l'inscription réussit, false sinon
     */
    public boolean signUp(String nom, String prenom, String email, String password, int alerte,String villeDefaut, String V1, String V2, String V3, String V4, String V5, String unite) {
        try {
            // Validation des données d'entrée
            if (nom == null || prenom == null || email == null || password == null || 
                nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
                LOGGER.log(Level.WARNING, "Tous les champs sont requis pour l'inscription.");
                return false;
            }

            if (!email.contains("@") || !email.contains(".")) {
                LOGGER.log(Level.WARNING, "Adresse email invalide : {0}", email);
                return false;
            }

            // Création d'un nouvel utilisateur
            User newUser = new User(0, nom, prenom, email, password, alerte);

            // Ajouter l'utilisateur, son historique et ses préférences
            boolean isAdded = databaseManager.createUserWithSearchHistoryAndPreferences(newUser, villeDefaut,  V1,  V2,  V3,  V4,  V5,  unite);

            if (isAdded) {
                LOGGER.log(Level.INFO, "Utilisateur inscrit avec succès : {0}", email);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Échec de l'inscription : Email déjà utilisé ou problème de base de données.");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'inscription : {0}", e.getMessage());
            return false;
        }
    }

	public boolean isCityValid(String city) {
	
		return databaseManager.isCityValid(city);
	}

}
