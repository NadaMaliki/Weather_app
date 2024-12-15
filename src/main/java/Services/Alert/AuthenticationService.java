package Services.Alert;

import View.MainApp;
import Dao.DBConnexion;
import Dao.DatabaseManager;
import Dao.UserPreferencesManager;
import Controller.SignInController;
import Controller.WeatherController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final SignInController signInController;
    private final DatabaseManager dbManager;
    private final DBConnexion dbConnection;

    public AuthenticationService() {
        this.signInController = new SignInController();
        this.dbConnection = new DBConnexion();
        this.dbManager = new DatabaseManager(dbConnection);
    }

    public void authenticateAndRedirect(String email, String password, Stage primaryStage) {
        try {
            if (!signInController.validateLogin(email, password)) {
                showError("Email ou mot de passe incorrect.");
                return;
            }

            int userId = authenticateUser(email, password);
            if (userId != -1) {
                handleSuccessfulAuthentication(userId, primaryStage);
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur d'authentification", e);
            showError("Erreur lors de la connexion : " + e.getMessage());
        }
    }

    private int authenticateUser(String email, String password) throws SQLException {
        String query = "SELECT id, password FROM utilisateur WHERE email = ?";
        try (Connection conn = dbConnection.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(password, rs.getString("password"))) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private void handleSuccessfulAuthentication(int userId, Stage primaryStage) {
        try {
            UserPreferencesManager preferencesManager = new UserPreferencesManager(dbConnection);
            // Récupérer directement la permission depuis MainApp
            boolean locationPermissionGranted = MainApp.isLocationPermissionGranted();
            LOGGER.info("Permission de localisation : " + locationPermissionGranted);

            loadWeatherScene(primaryStage, userId, locationPermissionGranted, preferencesManager);
            LOGGER.info("Authentification réussie pour l'utilisateur " + userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification", e);
            showError("Erreur lors de la connexion : " + e.getMessage());
        }
    }

    private void loadWeatherScene(Stage stage, int userId, boolean locationPermissionGranted,
                                  UserPreferencesManager preferencesManager) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Weather.fxml"));
            Parent root = loader.load();

            WeatherController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("Le contrôleur Weather n'a pas pu être chargé");
            }

            try {
                controller.setUserId(userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            controller.initialize();
            controller.initializeWithPreferences(locationPermissionGranted, preferencesManager);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            LOGGER.info("Interface météo chargée pour l'utilisateur " + userId);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface météo", e);
            throw e;
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}