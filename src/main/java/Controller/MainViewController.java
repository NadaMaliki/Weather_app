package Controller;

import View.SignIn;
import View.SignUp;
import Services.Api.GeolocationApi;
import Services.Api.WeatherApi;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;

import java.awt.*;

public class MainViewController {
    @FXML private VBox root;
    @FXML private TextField searchField;
    @FXML private Label weatherInfoLabel;
    @FXML private VBox weatherContent;
    @FXML private ImageView logoImageView;

    private final WeatherApi weatherApi = new WeatherApi();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/weather_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    private void initialize() {
        Image logoImage = new Image(getClass().getResourceAsStream("/images/weather_icon.png"));
        logoImageView.setImage(logoImage);

        setupUI();


    }

    private void setupUI() {
        // Configuration de la barre de recherche
        searchField.setPromptText("Entrez le nom d'une ville");
        searchField.setPrefWidth(200);

        // Configuration des événements de recherche
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });
    }

    @FXML
    private void handleSignIn() {
        try {
            SignIn signInPage = new SignIn();
            signInPage.start((Stage) root.getScene().getWindow());
        } catch (Exception ex) {
            showError("Erreur lors de l'ouverture de la page de connexion");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            SignUp signUpPage = new SignUp();
            System.out.println(signUpPage);
            signUpPage.start((Stage) root.getScene().getWindow());
        } catch (Exception ex) {
            showError("Erreur lors de l'ouverture de la page d'inscription");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String city = searchField.getText().trim();
        if (!city.isEmpty()) {
            displayWeather(city);
            displayHourlyWeather(city);
        }
    }

    private void askForLocationPermission() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Permission de localisation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous activer la localisation pour afficher les prévisions météo ?");

        ButtonType yesButton = new ButtonType("Oui");
        ButtonType noButton = new ButtonType("Non");
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                handleLocationPermissionGranted();
            } else {
                displayWeather("Rouen");
                displayHourlyWeather("Rouen");
            }
        });
    }

    private void handleLocationPermissionGranted() {
        GeolocationApi geolocationApi = new GeolocationApi();
        try {
            String geoInfo = geolocationApi.getGeolocationInfo();
            if (geoInfo != null && !geoInfo.isEmpty()) {
                String city = geolocationApi.parseCityFromGeolocationInfo(geoInfo);
                if (city != null && !city.isEmpty()) {
                    displayWeather(city);
                    displayHourlyWeather(city);
                } else {
                    handleGeolocationError();
                }
            } else {
                handleGeolocationError();
            }
        } catch (Exception e) {
            handleGeolocationError();
        }
    }

    private void handleGeolocationError() {
        showError("Impossible de déterminer votre localisation");
        displayWeather("Tanger");
        displayHourlyWeather("Tanger");
    }

    public void displayWeather(String city) {
        try {
            var weatherData = weatherApi.getCurrentWeatherByCity(city);
            if (weatherData != null) {
                updateWeatherDisplay(city, weatherInfoLabel, root);            } else {
                showError("Impossible de récupérer les données météo.");
            }
        } catch (Exception ex) {
            showError("Erreur lors de la récupération des données météo.");
            ex.printStackTrace();
        }
    }

    public void displayHourlyWeather(String city) {
        try {
            var hourlyData = weatherApi.getHourlyWeatherForecastByCity(city);
            if (hourlyData != null) {
                updateHourlyWeatherDisplay(city, root);
            } else {
                showError("Impossible de récupérer les prévisions horaires.");
            }
        } catch (Exception e) {
            showError("Erreur lors de l'affichage des prévisions horaires.");
            e.printStackTrace();
        }
    }
    private void updateWeatherDisplay(String city, Label weatherInfoLabel, VBox root) {
        try {
            var weatherData = weatherApi.getCurrentWeatherByCity(city);
            if (weatherData != null) {
                // Récupération des données météo
                String temp = weatherData.path("current").path("temp_c").asText(null);
                String feelsLike = weatherData.path("current").path("feelslike_c").asText(null);
                String humidity = weatherData.path("current").path("humidity").asText(null);
                String windSpeed = weatherData.path("current").path("wind_kph").asText(null);
                String condition = weatherData.path("current").path("condition").path("text").asText(null);
                String sunrise = weatherData.path("astro").path("sunrise").asText(null);
                String sunset = weatherData.path("astro").path("sunset").asText(null);
                String pressure = weatherData.path("current").path("pressure_mb").asText(null);
                String currentTime = java.time.LocalTime.now().withSecond(0).withNano(0).toString();

                if (temp != null && feelsLike != null && humidity != null && windSpeed != null && condition != null) {
                    // Clear seulement weatherContent au lieu de root
                    weatherContent.getChildren().clear();

                    // Création des éléments d'affichage
                    Label cityLabel = new Label(city);
                    cityLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");
                    Label timeLabel = new Label(currentTime);
                    timeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: lightgray;");

                    VBox cityTimeBox = new VBox(cityLabel, timeLabel);
                    cityTimeBox.setAlignment(Pos.CENTER);

                    Label tempLabel = new Label(temp + "°C");
                    tempLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold;");
                    Label feelsLikeLabel = new Label("Feels like: " + feelsLike + "°C");
                    feelsLikeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: lightgray;");

                    VBox tempBox = new VBox(tempLabel, feelsLikeLabel);
                    tempBox.setAlignment(Pos.CENTER);

                    HBox detailsBox = new HBox(20);
                    detailsBox.setAlignment(Pos.CENTER);
                    detailsBox.getChildren().addAll(
                            createWeatherDetail("Rain", "rainIcon.png", humidity + "%"),
                            createWeatherDetail("Wind", "windIcon.png", windSpeed + " km/h"),
                            createWeatherDetail("Humidity", "HumidityIcon.png", humidity + "%"),
                            createWeatherDetail("Pressure", "PressureIcon.png", pressure + " hPa")
                    );

                    Label sunriseLabel = new Label("Sunrise: " + sunrise);
                    sunriseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgray;");
                    Label sunsetLabel = new Label("Sunset: " + sunset);
                    sunsetLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgray;");
                    HBox sunBox = new HBox(20, sunriseLabel, sunsetLabel);
                    sunBox.setAlignment(Pos.CENTER);

                    // Ajouter les éléments au weatherContent au lieu de root
                    weatherContent.getChildren().addAll(cityTimeBox, tempBox, detailsBox, sunBox);
                    weatherContent.setSpacing(20);
                    weatherContent.setAlignment(Pos.CENTER);

                    // Mettre à jour l'arrière-plan
                    setBackgroundByCondition(root, condition.toLowerCase());
                }
            }
        } catch (Exception ex) {
            showError("Erreur lors de la récupération des données météo.");
            ex.printStackTrace();
        }
    }

    private void updateHourlyWeatherDisplay(String city, VBox root) {

        try {
            JsonNode hourlyData = weatherApi.getHourlyWeatherForecastByCity(city);
            if (hourlyData != null) {
                // Créer la HBox pour les prévisions horaires
                HBox hourlyHBox = new HBox(30);
                hourlyHBox.setAlignment(Pos.CENTER_LEFT);
                hourlyHBox.setPadding(new Insets(10));

                for (JsonNode hour : hourlyData) {
                    String time = hour.path("time").asText();
                    String temp = hour.path("temp_c").asText();
                    String condition = hour.path("condition").path("text").asText();
                    String iconUrl = hour.path("condition").path("icon").asText();
                    String formattedTime = time.substring(11, 13) + "h";

                    Label timeLabel = new Label(formattedTime);
                    timeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                    Label tempLabel = new Label(temp + "°C - " + condition);
                    tempLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

                    ImageView iconView = new ImageView(new Image("http:" + iconUrl));
                    iconView.setFitWidth(30);
                    iconView.setFitHeight(30);

                    VBox hourBox = new VBox(5, timeLabel, tempLabel, iconView);
                    hourBox.setAlignment(Pos.CENTER);
                    hourBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-padding: 10px; -fx-background-color: #f9f9f9;");

                    // Effet hover
                    hourBox.setOnMouseEntered(event -> hourBox.setStyle("-fx-border-color: #0078d7; -fx-background-color: #e0f7ff;"));
                    hourBox.setOnMouseExited(event -> hourBox.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f9f9f9;"));

                    hourlyHBox.getChildren().add(hourBox);
                }

                // Configurer le ScrollPane
                ScrollPane scrollPane = new ScrollPane(hourlyHBox);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefHeight(150); // Hauteur fixe
                scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

                // Ajouter le ScrollPane au weatherContent au lieu du root
                weatherContent.getChildren().add(scrollPane);

                // Définir la largeur maximale du ScrollPane
                scrollPane.setMaxWidth(root.getWidth() * 0.9); // 90% de la largeur du root

                // Mettre à jour la largeur du ScrollPane quand la fenêtre change de taille
                root.widthProperty().addListener((obs, oldVal, newVal) -> {
                    scrollPane.setMaxWidth(newVal.doubleValue() * 0.9);
                });
            }
        } catch (Exception e) {
            showError("Erreur lors de l'affichage des prévisions horaires.");
            e.printStackTrace();
        }
    }


    private void setBackgroundByCondition(VBox root, String condition) {
        String imageFileName;

        // Associez les conditions météo à vos images
        if (condition.contains("sun") || condition.contains("clear")) {
            imageFileName = "sunny.jpg";
        } else if (condition.contains("rain")) {
            imageFileName = "Pluit.jpg";
        } else if (condition.contains("cloud")) {
            imageFileName = "nuageux.jpg";
        } else if (condition.contains("snow")) {
            imageFileName = "snow2.png";
        } else if (condition.contains("thunderstorm")) {
            imageFileName = "Tonnere_clair.jpg";
        } else if (condition.contains("Light rain")) {
            imageFileName = "Lightrain.png";
        } else if (condition.contains("overcast")) {
            imageFileName = "overcast.jpg";
        } else {
            imageFileName = "default.jpg"; // Image par défaut
        }

        // Charger l'image à partir du classpath
        try {
            Image backgroundImage = new Image(getClass().getResource("/images/" + imageFileName).toString());
            // Spécifier que l'image doit être étalée pour couvrir toute la zone de la fenêtre
            BackgroundImage bg = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO,
                            true, true, false, true));
            root.setBackground(new Background(bg));
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'image : " + imageFileName);
        }
    }


    // Méthode pour créer l'affichage des détails météo avec des icônes
    private VBox createWeatherDetail(String label, String iconFileName, String value) {
        String iconPath = "/images/" + iconFileName;
        Image icon = new Image(getClass().getResourceAsStream(iconPath));
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(32);
        iconView.setFitHeight(32);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: lightgray;");

        VBox detailBox = new VBox(5, iconView, valueLabel, nameLabel);
        detailBox.setAlignment(Pos.CENTER);
        return detailBox;
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}