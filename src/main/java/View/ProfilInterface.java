package View;
import Controller.ProfilController;
import Controller.WeatherController;
import Model.Session;

import Dao.DatabaseManager;
import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import Model.User;
import Model.UserPreferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProfilInterface extends Application {

    private final DatabaseManager databaseManager = new DatabaseManager(new DBConnexion());
    private final UserPreferencesManager preferencesManager = new UserPreferencesManager(new DBConnexion());
    private final UserPreferences preferences = new UserPreferences(preferencesManager);
    private final ProfilController profilController = new ProfilController(databaseManager, preferencesManager, preferences);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Vérifie si l'utilisateur est connecté
        if (!Session.isLoggedIn()) {
            System.out.println("Aucun utilisateur connecté. Redirection vers la page de connexion.");
            SignIn signInPage = new SignIn();
            signInPage.start(primaryStage);
            return;
        }

        // Récupérer les informations utilisateur
        User currentUser = profilController.getCurrentUser();
        int userId = currentUser.getId();
        String unit = profilController.getUserUnit(userId);
        boolean alertPreference = profilController.getUserAlertPreference(userId);

        // Récupérer les villes
        String defaultCity = preferencesManager.getDefaultCity(userId);
        List<String> favoriteCities = profilController.getFavoriteCities(userId);

        // Barre en haut
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color: #0B3D91;");
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
//Retour vers la page utilisateur
        Button btnRetour = new Button("Retour");
        btnRetour.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        btnRetour.setOnAction(e -> {
            try {
                // Charger Weather.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Weather.fxml"));
                Parent root = loader.load();

                DBConnexion dbConnexion = new DBConnexion();
                UserPreferencesManager userpreferences = new UserPreferencesManager(dbConnexion);


                WeatherController controller = loader.getController();
                System.out.println(Session.getCurrentUser().getId());
                try {
                    controller.setUserId(Session.getCurrentUser().getId());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                controller.displayWeatherDetailsForCity(userpreferences.getDefaultCity(Session.getCurrentUser().getId()));
                controller.displayGraphManager.displayWeatherGraphs(userpreferences.getDefaultCity(Session.getCurrentUser().getId()));

              
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/weather.css").toExternalForm());
                primaryStage.setScene(scene);
                primaryStage.show();

            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Erreur lors du chargement de Weather.fxml: " + ex.getMessage());
            }
        });

        Button btnDeconnecter = new Button("Se déconnecter");
        btnDeconnecter.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        btnDeconnecter.setOnAction(e -> {
            Session.logout();
            SignIn signInPage = new SignIn();
            try {
                signInPage.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        topBar.getChildren().addAll(btnRetour, btnDeconnecter);

        // Image de profil
        ImageView profileImage = new ImageView(new Image(getClass().getResourceAsStream("/inscri.png")));
        profileImage.setFitWidth(80);
        profileImage.setFitHeight(80);

        VBox imageContainer = new VBox(profileImage);
        imageContainer.setAlignment(Pos.TOP_LEFT);
        imageContainer.setPadding(new Insets(10));

        // Informations utilisateur
        VBox infoContainer = new VBox(5);
        infoContainer.setPadding(new Insets(10));
        infoContainer.setStyle("-fx-border-color: #0B3D91; -fx-border-width: 2; -fx-background-color: #F0F8FF;");
        infoContainer.setAlignment(Pos.TOP_LEFT);
        String Alerte;
        int res;
        res = currentUser.isAlerte();

        switch (res) {
            case 0:
                Alerte = "Aucune alerte n'a été choisie";
                break;
            case 1:
                Alerte = "Email";
                break;
            case 2:
                Alerte = "Notification";
                break;
            case 3:
                Alerte = "Email et Notification";
                break;
            default:
                Alerte = "Valeur inconnue";
                break;
        }

         
        Text nameText = createStyledText("Nom : " + currentUser.getNom());
        Text prenomText = createStyledText("Prénom : " + currentUser.getPrenom());
        Text emailText = createStyledText("Email : " + currentUser.getEmail());
        Text unitText = createStyledText("Unité : " + unit);
        Text alertText = createStyledText("Type d'alerte : " +  Alerte);

        Button btnModifierProfil = new Button("Modifier profil");
        btnModifierProfil.setOnAction(e -> {
            UpdateProfilInterface updateProfil = new UpdateProfilInterface();
            try {
                updateProfil.start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        infoContainer.getChildren().addAll(nameText, prenomText, emailText, unitText, alertText, btnModifierProfil);

        // Tableau des villes
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(20));
        tableContainer.setStyle("-fx-border-color: #0B3D91; -fx-border-width: 2; -fx-background-color: #F0F8FF;");

        Text tableTitle = createStyledText("Villes :");
        tableContainer.getChildren().add(tableTitle);

        // Ville par défaut
        HBox defaultCityRow = new HBox(10);
        defaultCityRow.setAlignment(Pos.CENTER_LEFT);
        Text defaultCityText = new Text("Ville par défaut : " + defaultCity);

        // TextField et bouton Modifier pour la ville par défaut
        TextField defaultCityTextField = new TextField();
        defaultCityTextField.setVisible(false);
        
        Label errorLabel1 = new Label();
        errorLabel1.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel1.setVisible(false);
        Button btnDefaultCityOk = new Button("OK");
        btnDefaultCityOk.setVisible(false);
        btnDefaultCityOk.setOnAction(e -> {
            try {
            System.out.println(defaultCity);
            	if(!profilController.isCityValid(defaultCityTextField.getText())) {
              		 errorLabel1.setText("La ville est invalide.");
                       errorLabel1.setVisible(true);
                       return;
                       }
                    	   profilController.updateDefaultCity(userId, defaultCityTextField.getText());
                           start(primaryStage); // Rafraîchit l'interface
                       
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button btnDefaultCityModifier = new Button("Modifier");
        btnDefaultCityModifier.setOnAction(e -> {
        	
            defaultCityTextField.setVisible(true);
            btnDefaultCityOk.setVisible(true);
            defaultCityTextField.setText(defaultCity);
            
        });

        defaultCityRow.getChildren().addAll(defaultCityText, defaultCityTextField, btnDefaultCityOk, btnDefaultCityModifier);
        tableContainer.getChildren().add(defaultCityRow);

        
        GridPane cityTable = new GridPane();
        cityTable.setHgap(10);
        cityTable.setVgap(10);
        cityTable.setPadding(new Insets(10));

        for (int i = 0; i < 5; i++) {
            String city = i < favoriteCities.size() ? favoriteCities.get(i) : null;

            Text cityText = new Text("Ville " + (i + 1) + " : " + (city != null ? city : "Vide"));
            TextField textField = new TextField();
            textField.setVisible(false); 

            Label errorLabel = new Label();
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            errorLabel.setVisible(false);
            
            Button btnOk = new Button("OK");
            btnOk.setVisible(false); 
            final int currentIndex = i + 1;
            btnOk.setOnAction(e -> {
                try {
                	if(!profilController.isCityValid(textField.getText())) {
                		 errorLabel.setText("La ville est invalide.");
                         errorLabel.setVisible(true);
                         return;
                	}
                    profilController.updateFavoriteCity( userId,currentIndex, textField.getText());
                    System.out.println(userId);
                    System.out.println(currentIndex);
                    System.out.println(textField.getText());
                    start(primaryStage); 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Button btnModifier = new Button("Modifier");
            btnModifier.setOnAction(e -> {
                textField.setVisible(true);
                btnOk.setVisible(true);
                textField.setText(city != null ? city : "");
            });

            Button btnSupprimer = new Button("Supprimer");
            btnSupprimer.setOnAction(e -> {
                try {
                    profilController.removeFavoriteCity(userId, currentIndex);
                    start(primaryStage); 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            cityTable.add(cityText, 0, i);
            cityTable.add(textField, 1, i);
            cityTable.add(btnOk, 2, i);
            cityTable.add(btnModifier, 3, i);
            cityTable.add(btnSupprimer, 4, i);
        }

        tableContainer.getChildren().add(cityTable);

        // Conteneur principal
        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_LEFT);
        mainContent.getChildren().addAll(imageContainer, infoContainer, tableContainer);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(mainContent);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Profil Utilisateur");
        primaryStage.show();
    }

    private Text createStyledText(String content) {
        Text text = new Text(content);
        text.setFont(Font.font("Arial", 14));
        text.setFill(Color.DARKBLUE);
        text.setStyle("-fx-font-weight: bold;");
        return text;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
