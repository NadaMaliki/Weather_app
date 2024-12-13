package Controller;

import Dao.DatabaseManager;
import Dao.DBConnexion;
import Model.User;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignInController {

    private final DatabaseManager databaseManager;
    private static final Logger LOGGER = Logger.getLogger(SignInController.class.getName());

    public SignInController() {
        DBConnexion dbConnexion = new DBConnexion(); // Initialisation de la connexion
        this.databaseManager = new DatabaseManager(dbConnexion);
    }

    public boolean validateLogin(String email, String password) {
        try {
            if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Champs email ou mot de passe vides.");
                return false;
            }

            // Utilise la méthode isUserAuthenticated pour vérifier les informations d'identification
            boolean isAuthenticated = databaseManager.isUserAuthenticated(email, password);

            if (isAuthenticated) {
                LOGGER.log(Level.INFO, "Utilisateur authentifié avec succès : {0}", email);

                // Récupère les informations de l'utilisateur et les enregistre dans la session
                User user = databaseManager.getUserByEmail(email);
                if (user != null) {
                    Session.setCurrentUser(user);
                } else {
                    LOGGER.log(Level.WARNING, "Impossible de récupérer l'utilisateur après l'authentification : {0}", email);
                    return false;
                }

                return true;
            } else {
                LOGGER.log(Level.WARNING, "Authentification échouée pour : {0}", email);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la validation de connexion : {0}", e.getMessage());
            return false;
        }
    }
}
