package Controller;

import DAO.DatabaseManager;
import Model.User;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateProfilInterfaceController {

    private final DatabaseManager databaseManager;
    private static final Logger LOGGER = Logger.getLogger(UpdateProfilInterfaceController.class.getName());

    public UpdateProfilInterfaceController(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean updateUserProfile(User user) {
        try {
            if (databaseManager.updateUser(user)) {
                LOGGER.log(Level.INFO, "Profil utilisateur mis à jour avec succès : {0}", user.getEmail());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Échec de la mise à jour du profil utilisateur : {0}", user.getEmail());
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la mise à jour du profil utilisateur : {0}", e.getMessage());
            return false;
        }
    }
}
