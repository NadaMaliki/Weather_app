package View;

import Controller.SignInController;
import Model.Session;
import Model.User;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import Services.Alert.AuthenticationService;

import java.util.Objects;

public class SignIn extends Application {

    private final SignInController controller = new SignInController();
    AuthenticationService authService = new AuthenticationService();
    @Override
    public void start(Stage primaryStage) {
        int width = 800;
        int height = 600;

        Image backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/APS.png")));
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        StackPane root = new StackPane();
        root.setBackground(new Background(bgImage));

        // Barre horizontale en haut
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #0B3D91;");
        topBar.setPadding(new Insets(5, 15, 5, 15));
        topBar.setSpacing(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Bouton "Retour"
        Button backButton = new Button("Retour");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        backButton.setOnAction(e -> {
            MainApp MainAppPage = new MainApp();
            try {
            	MainAppPage.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Bouton "S'inscrire"
        Button signUpButton = new Button("S'inscrire");
        signUpButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        signUpButton.setOnAction(e -> {
            SignUp signUpPage = new SignUp();
            try {
                signUpPage.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Ajouter les boutons à la barre
        topBar.getChildren().addAll(backButton, signUpButton);

        Circle profileCircle = new Circle(75);
        profileCircle.setFill(Color.WHITE);

        Image profileImage = new Image(getClass().getResourceAsStream("/inscri.png"));
        profileCircle.setFill(new ImagePattern(profileImage));

        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 30; -fx-background-color: white; -fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
        formContainer.setMaxWidth(400);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Entrez votre email");

        Label passwordLabel = new Label("Mot de passe:");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button validateButton = new Button("Se connecter");
        validateButton.setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white; -fx-font-size: 14px;");
        validateButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            boolean success = controller.validateLogin(email, password);
            if (success) {
                errorLabel.setVisible(false);
                User currentUser = Session.getCurrentUser();
                System.out.println("Utilisateur connecté : " + currentUser.getEmail());
//adapter la redirection
                authService.authenticateAndRedirect(email,password,primaryStage);
            } else {
                errorLabel.setText("Email ou mot de passe incorrect !");
                errorLabel.setVisible(true);
            }
        });

        formContainer.getChildren().addAll(emailLabel, emailField, passwordLabel, passwordField, validateButton, errorLabel);

        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getChildren().addAll(profileCircle, formContainer);

        VBox rootContainer = new VBox();
        rootContainer.setAlignment(Pos.TOP_CENTER);
        rootContainer.getChildren().addAll(topBar, mainContainer);

        VBox.setVgrow(mainContainer, Priority.ALWAYS);
        root.getChildren().add(rootContainer);

        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("Page de connexion");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
