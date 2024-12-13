package View;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SignUp extends Application {

    private final SignUpController controller = new SignUpController();

    @Override
    public void start(Stage primaryStage) {
        int width = 800;
        int height = 600;

        // Charger l'image d'arrière-plan
        Image backgroundImage = new Image(getClass().getResourceAsStream("/SUP.png"));

        
        BackgroundImage bgImage = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        // Conteneur principal avec l'arrière-plan
        StackPane root = new StackPane();
        root.setBackground(new Background(bgImage));

        // Barre horizontale en haut
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #0B3D91;");
        topBar.setPadding(new Insets(25, 15, 5, 15));
        topBar.setSpacing(50);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Bouton "Retour"
        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px;");
        backButton.setOnAction(e -> {
            MainApp scene = new MainApp();
            try {
                scene.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Bouton "Se connecter"
        Button signInButton = new Button("Se connecter");
        signInButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px;");
        signInButton.setOnAction(e -> {
            SignIn signIn = new SignIn();
            try {
                signIn.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Ajouter les boutons à la barre
        topBar.getChildren().addAll(backButton, signInButton);

        // Conteneur pour le formulaire
        VBox formContainer = new VBox(10);
        formContainer.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
        formContainer.setMaxWidth(500);

        // Champs du formulaire
        Label nameLabel = new Label("Nom:");
        TextField nameField = new TextField();
        nameField.setPromptText("Entrez votre nom");

        Label surnameLabel = new Label("Prénom:");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Entrez votre prénom");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Entrez votre email");

        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");

        Label defaultCityLabel = new Label("Ville par défaut:");
        TextField defaultCityField = new TextField();
        defaultCityField.setPromptText("Entrez votre ville par défaut");

     // Conteneur pour les villes de préférence
        Label preferredCitiesLabel = new Label("Villes de préférence:");

        GridPane preferredCitiesGrid = new GridPane();
        preferredCitiesGrid.setHgap(10);
        preferredCitiesGrid.setVgap(10);

        TextField city1Field = new TextField();
        city1Field.setPromptText("Ville 1");

        TextField city2Field = new TextField();
        city2Field.setPromptText("Ville 2");

        TextField city3Field = new TextField();
        city3Field.setPromptText("Ville 3");

        TextField city4Field = new TextField();
        city4Field.setPromptText("Ville 4");

        TextField city5Field = new TextField();
        city5Field.setPromptText("Ville 5");

        // Ajout des champs dans le GridPane
        preferredCitiesGrid.add(city1Field, 0, 0);
        preferredCitiesGrid.add(city2Field, 1, 0);
        preferredCitiesGrid.add(city3Field, 0, 1);
        preferredCitiesGrid.add(city4Field, 1, 1);
        preferredCitiesGrid.add(city5Field, 0, 2);


       

        Label unitLabel = new Label("Unité:");
        ChoiceBox<String> unitChoiceBox = new ChoiceBox<>();
        unitChoiceBox.getItems().addAll("°C", "°K", "°F");
        unitChoiceBox.setValue("°C");

        // Cases à cocher pour le mode d'alerte
        Label alertLabel = new Label("Mode d'alerte:");
        CheckBox emailAlert = new CheckBox("Email");
        CheckBox notificationAlert = new CheckBox("Notification");

        HBox alertOptions = new HBox(10, emailAlert, notificationAlert);

        // Message d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        // Bouton "S'inscrire"

        Button signUpButton = new Button("S'inscrire");
        signUpButton.setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px;");
        signUpButton.setOnAction(e -> {
            String nom = nameField.getText().trim();
            String prenom = surnameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String defaultCity = defaultCityField.getText().trim();
            String city1 = city1Field.getText().trim();
            String city2 = city2Field.getText().trim();
            String city3 = city3Field.getText().trim();
            String city4 = city4Field.getText().trim();
            String city5 = city5Field.getText().trim();
            String unit = unitChoiceBox.getValue();

            // Validation des villes
            if (defaultCity.isEmpty() || !controller.isCityValid(defaultCity)) {
                errorLabel.setText("La ville par défaut est invalide.");
                errorLabel.setVisible(true);
                return;
            }
            if (!city1.isEmpty() && !controller.isCityValid(city1)) {
                errorLabel.setText("La ville 1 est invalide.");
                errorLabel.setVisible(true);
                return;
            }
            if (!city2.isEmpty() && !controller.isCityValid(city2)) {
                errorLabel.setText("La ville 2 est invalide.");
                errorLabel.setVisible(true);
                return;
            }
            if (!city3.isEmpty() && !controller.isCityValid(city3)) {
                errorLabel.setText("La ville 3 est invalide.");
                errorLabel.setVisible(true);
                return;
            }
            if (!city4.isEmpty() && !controller.isCityValid(city4)) {
                errorLabel.setText("La ville 4 est invalide.");
                errorLabel.setVisible(true);
                return;
            }
            if (!city5.isEmpty() && !controller.isCityValid(city5)) {
                errorLabel.setText("La ville 5 est invalide.");
                errorLabel.setVisible(true);
                return;
            }

            // Calculer la valeur de l'alerte
            int alerte = 0;
            if (emailAlert.isSelected() && notificationAlert.isSelected()) {
                alerte = 3;
            } else if (emailAlert.isSelected()) {
                alerte = 1;
            } else if (notificationAlert.isSelected()) {
                alerte = 2;
            }

            // Inscription
            boolean success = controller.signUp(nom, prenom, email, password, alerte, defaultCity, city1, city2, city3, city4, city5, unit);
            if (success) {
                errorLabel.setVisible(false);
                System.out.println("Inscription réussie !");
                SignIn app = new SignIn();
                try {
                    app.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                errorLabel.setText("Erreur lors de l'inscription. Veuillez réessayer.");
                errorLabel.setVisible(true);
            }
        });


        // Ajouter les éléments au formulaire
        formContainer.getChildren().addAll(
            nameLabel, nameField,
            surnameLabel, surnameField,
            emailLabel, emailField,
            passwordLabel, passwordField,
            defaultCityLabel, defaultCityField,
            preferredCitiesGrid, preferredCitiesLabel,
            unitLabel, unitChoiceBox,
            alertLabel, alertOptions,
            signUpButton,
            errorLabel
        );

        // Conteneur principal
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(formContainer);

        // Conteneur global
        VBox rootContainer = new VBox();
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getChildren().addAll(topBar, mainContainer);

        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        root.getChildren().add(rootContainer);

        // Création de la scène
        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("Page d'inscription");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
