package View;

import Dao.DBConnexion;
import Dao.DatabaseManager;
import Model.Session;
import Model.User;
import Model.UserPreferences;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Controller.*;
public class UpdateProfilInterface extends Application {

    private final DatabaseManager databaseManager = new DatabaseManager(new DBConnexion());
    private final UpdateProfilInterfaceController controller = new UpdateProfilInterfaceController(databaseManager);
    private final UserPreferences preferences = new UserPreferences(new Dao.UserPreferencesManager(new DBConnexion()));

    @Override
    public void start(Stage primaryStage) {
        if (!Session.isLoggedIn()) {
            System.out.println("Aucun utilisateur connecté. Redirection vers la page de connexion.");
            new SignIn().start(primaryStage);
            return;
        }

        User currentUser = Session.getCurrentUser();
        int userId = currentUser.getId();

        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.CENTER);

        TextField nameField = new TextField(currentUser.getNom());
        nameField.setPromptText("Nom");

        TextField prenomField = new TextField(currentUser.getPrenom());
        prenomField.setPromptText("Prénom");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe");

        Label alertInfoLabel = new Label("Choisissez les types d'alerte que vous souhaitez recevoir :");
        alertInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        CheckBox emailAlertCheckBox = new CheckBox("Email");
        CheckBox notificationAlertCheckBox = new CheckBox("Notification");

        Label unitLabel = new Label("Choisissez votre unité :");
        unitLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ChoiceBox<String> unitChoiceBox = new ChoiceBox<>();
        unitChoiceBox.getItems().addAll("°C", "°K", "°F");
        unitChoiceBox.setValue(preferences.getUnit(userId));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            try {
                // Validation du champ mot de passe
                if (passwordField.getText().trim().isEmpty()) {
                    errorLabel.setText("Le mot de passe est obligatoire.");
                    errorLabel.setVisible(true);
                    return;
                }

                currentUser.setNom(nameField.getText());
                currentUser.setPrenom(prenomField.getText());
                currentUser.setEmail(emailField.getText());
                currentUser.setPassword(passwordField.getText());

                int alertValue = (emailAlertCheckBox.isSelected() ? 1 : 0) +
                        (notificationAlertCheckBox.isSelected() ? 2 : 0);
                currentUser.setAlerte(alertValue);

                boolean profileUpdated = controller.updateUserProfile(currentUser);
                boolean unitUpdated = preferences.editUnit(userId, unitChoiceBox.getValue());

                if (profileUpdated && unitUpdated) {
                    errorLabel.setVisible(false);
                    showSuccessAlert("Vos modifications ont été enregistrées avec succès !");
                } else {
                    showErrorAlert("Échec de la sauvegarde des modifications.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorAlert("Une erreur est survenue lors de la sauvegarde des modifications.");
            }
        });

        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;");
        backButton.setOnAction(e -> {
            try {
                new ProfilInterface().start(primaryStage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        GridPane formLayout = new GridPane();
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setVgap(10);
        formLayout.setHgap(10);

        formLayout.add(new Label("Nom:"), 0, 0);
        formLayout.add(nameField, 1, 0);

        formLayout.add(new Label("Prénom:"), 0, 1);
        formLayout.add(prenomField, 1, 1);

        formLayout.add(new Label("Email:"), 0, 2);
        formLayout.add(emailField, 1, 2);

        formLayout.add(new Label("Mot de passe:"), 0, 3);
        formLayout.add(passwordField, 1, 3);

        formLayout.add(alertInfoLabel, 0, 4, 2, 1);
        formLayout.add(emailAlertCheckBox, 0, 5);
        formLayout.add(notificationAlertCheckBox, 1, 5);

        formLayout.add(unitLabel, 0, 6);
        formLayout.add(unitChoiceBox, 1, 6);

        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(saveButton, backButton);

        mainContainer.getChildren().addAll(formLayout, errorLabel, buttonContainer);

        Scene scene = new Scene(mainContainer, 600, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Modifier le profil");
        primaryStage.show();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
