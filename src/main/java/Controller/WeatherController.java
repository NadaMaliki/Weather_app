package Controller;


import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import Model.UserPreferences;
import Model.ProfilInterface;
import Services.Alert.AlerteService;
import Services.Alert.NotificationService;
import Services.Graph.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import Services.Api.GeolocationApi;
import Services.Api.WeatherApi;
import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import Model.SearchHistory;
import Dao.DatabaseManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherController {




    @FXML private VBox currentWeatherBox;
    @FXML private Label currentTempLabel;
    @FXML private Label currentConditionLabel;
    @FXML private ImageView currentWeatherIcon;
    private String currentCity;
    @FXML private VBox forecastWeatherBox;
    @FXML private VBox searchHistoryBox; // VBox to display search history
    @FXML private TextField searchCityTextField; // Field for city search
    @FXML private HBox hourlyForecastBox; // VBox to display hourly forecast




    private WeatherApi weatherApi = new WeatherApi(); // Weather API instance
    private SearchHistory searchHistory; // Search history instance
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(WeatherController.class.getName());


    public int userId;
    private DatabaseManager databaseManager;
    public void setUserId(int userId) {
        this.userId = userId;

        loadUserPreferences();
        // Réinitialiser le DisplayGraphManager avec le nouveau userId
        displayGraphManager = new DisplayGraphManager(
                temperatureChartContainer,
                rainChartContainer,
                windChartContainer,
                userId
        );
        searchHistory = new SearchHistory(userId, databaseManager);


        logger.info("Utilisateur ID : " + userId);
    }
    private void loadUserPreferences() {
        try {
            DBConnexion dbConnexion = new DBConnexion();
            preferencesManager = new UserPreferencesManager(dbConnexion);
            userPreferences = new UserPreferences(userId, dbConnexion);
            loadFavoriteCities();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement des préférences: " + e.getMessage());
        }
    }
    public WeatherController() {
        DBConnexion dbConnexion = new DBConnexion(); // If DBConnexion requires parameters, provide them
        databaseManager = new DatabaseManager(dbConnexion); // Pass DBConnexion to DatabaseManager
        searchHistory = new SearchHistory(userId, databaseManager); // Initialize search history
    }


    @FXML
    public void onCitySearchInput() {
        String searchInput = searchCityTextField.getText().trim(); // Récupère le texte saisi par l'utilisateur

        try {

            List<String> history = searchHistory.getHistory();

            // Limiter à 5 derniers éléments
            int maxHistorySize = 5;
            if (history.size() > maxHistorySize) {
                history = history.subList(history.size() - maxHistorySize, history.size());
            }

            if (!searchInput.isEmpty()) {
                // Filtrer l'historique pour n'afficher que les villes qui commencent par la saisie
                List<String> filteredHistory = history.stream()
                        .filter(city -> city.toLowerCase().startsWith(searchInput.toLowerCase()))
                        .collect(Collectors.toList());

                updateSearchHistoryDisplay(filteredHistory); // Afficher l'historique filtré
            } else {
                // Si rien n'est saisi, afficher l'historique limité
                updateSearchHistoryDisplay(history); // Afficher l'historique limité à 5
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération de l'historique.");
        }
    }

    public void updateSearchHistoryDisplay(List<String> history) {
        searchHistoryBox.getChildren().clear(); // Efface l'affichage précédent

        // Si l'historique est vide, afficher un message
        if (history.isEmpty()) {
            Label emptyHistoryLabel = new Label("Aucune ville dans l'historique.");
            searchHistoryBox.getChildren().add(emptyHistoryLabel);
            return;
        }


        for (String city : history) {
            Button cityButton = new Button(city);
            cityButton.setOnAction(event -> {
                handleCitySearchFromHistory(city);
            });

            // Style pour chaque bouton
            cityButton.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-font-size: 14; -fx-text-fill: #333; -fx-border-color: #ccc; -fx-border-radius: 5;");
            cityButton.setMaxWidth(Double.MAX_VALUE);


            searchHistoryBox.getChildren().add(cityButton);
        }
    }
    public void handleCitySearchFromHistory(String city) {

        searchCityTextField.setText(city);

        displayWeatherDetailsForCity(city);
        displayGraphManager.displayWeatherGraphs(city);

    }



    @FXML
    public void handleCitySearch() {
        String city = searchCityTextField.getText().trim();

        if (!city.isEmpty()) {

            displayCurrentWeather(city);
            displayMonthlyForecast(city);
            displayWeatherHourly(city);
            displayMonthlyForecast(city);
            displayGraphManager.displayWeatherGraphs(city);

            try {
                // Vérifie si la ville est déjà dans l'historique des recherches
                List<String> history = searchHistory.getHistory();
                // Ajouter la ville au début de l'historique si elle n'est pas déjà présente
                if (!history.contains(city)) {
                    searchHistory.addCityToHistory(userId, city);
                }

                // Rafraîchir l'affichage de l'historique
                refreshSearchHistory();
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de l'ajout ou de la récupération de l'historique.");
            }
        } else {
            showError("Veuillez entrer une ville valide.");
        }
    }

    private void refreshSearchHistory() {
        try {
            List<String> history = searchHistory.getHistory();
            updateSearchHistoryDisplay(history);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération de l'historique des recherches.");
        }

    }
    public void displayWeatherForecast(String city) {
        try {
            // Récupérer les données de prévision météo via l'API
            JsonNode forecastData = weatherApi.getWeatherForecastByCity(city);

            if (forecastData != null) {
                JsonNode forecastDays = forecastData.path("forecast").path("forecastday");

                // Nettoyer l'ancien contenu
                forecastWeatherBox.getChildren().clear();

                // Conteneur principal horizontal pour les prévisions des 5 jours
                HBox forecastContainer = new HBox();
                forecastContainer.setSpacing(15);
                forecastContainer.setPadding(new Insets(10));
                forecastContainer.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

                // Variable pour savoir quel jour est le lendemain
                LocalDate today = LocalDate.now();
                LocalDate forecastStartDate = today.plusDays(1);

                // Ajouter les prévisions pour les 5 jours suivants
                int dayCount = 0; // Compteur pour limiter à 5 jours
                for (int i = 0; i < 5; i++) {
                    LocalDate targetDate = forecastStartDate.plusDays(i);
                    JsonNode day = null;

                    // Trouver la prévision correspondant à la date cible
                    for (JsonNode forecast : forecastDays) {
                        LocalDate forecastDate = LocalDate.parse(forecast.path("date").asText());
                        if (forecastDate.equals(targetDate)) {
                            day = forecast;
                            break;
                        }
                    }

                    if (day != null) {
                        // Extraire les données pour la journée
                        int minTemp = day.path("day").path("mintemp_c").asInt();
                        int maxTemp = day.path("day").path("maxtemp_c").asInt();
                        String condition = day.path("day").path("condition").path("text").asText();
                        String iconUrl = day.path("day").path("condition").path("icon").asText();

                        // Icône météo
                        ImageView iconView = new ImageView(new Image("http:" + iconUrl));
                        iconView.setFitWidth(50);
                        iconView.setFitHeight(50);

                        // Libellés pour le jour
                        Label dateLabel = new Label(targetDate.getDayOfWeek().toString() + " " + targetDate.getDayOfMonth() + " " + targetDate.getMonth().toString().substring(0, 3));
                        Label tempLabel = new Label(minTemp + "°C / " + maxTemp + "°C");
                        Label conditionLabel = new Label(condition);

                        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
                        tempLabel.setStyle("-fx-font-size: 12px;");
                        conditionLabel.setStyle("-fx-font-size: 12px;");

                        // Conteneur vertical pour un jour
                        VBox dayBox = new VBox(5, dateLabel, iconView, tempLabel, conditionLabel);
                        dayBox.setAlignment(Pos.CENTER);
                        dayBox.setPadding(new Insets(10));
                        dayBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #fff;");

                        // Ajouter le conteneur pour le jour au conteneur horizontal
                        forecastContainer.getChildren().add(dayBox);
                    } else {
                        // Si aucune prévision n'est trouvée, afficher un message générique
                        Label missingDataLabel = new Label("Pas de données");
                        missingDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
                        VBox emptyDayBox = new VBox(5, new Label(targetDate.getDayOfWeek().toString()), missingDataLabel);
                        emptyDayBox.setAlignment(Pos.CENTER);
                        emptyDayBox.setPadding(new Insets(10));
                        emptyDayBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
                        forecastContainer.getChildren().add(emptyDayBox);
                    }

                    dayCount++;
                    if (dayCount >= 5) break;
                }

                // Ajouter le conteneur principal des prévisions dans le VBox
                forecastWeatherBox.getChildren().add(forecastContainer);
            } else {
                showError("Impossible de récupérer les prévisions météo.");
            }
        } catch (Exception e) {
            showError("Erreur lors de l'affichage des prévisions météo.");
            e.printStackTrace();
        }
    }


    public void displayCurrentWeather(String city) {
        try {
            JsonNode currentWeather = weatherApi.getCurrentWeatherByCity(city);

            if (currentWeather != null) {
                // Extraction des données de base
                int currentTemp = currentWeather.path("current").path("temp_c").asInt();
                double feelsLike = currentWeather.path("current").path("feelslike_c").asDouble();
                String condition = currentWeather.path("current").path("condition").path("text").asText();
                String weatherIcon = currentWeather.path("current").path("condition").path("icon").asText();
                int humidity = currentWeather.path("current").path("humidity").asInt();
                int pressure = currentWeather.path("current").path("pressure_mb").asInt();
                double rainChance = currentWeather.path("current").path("precip_mm").asDouble();

                // Nouvelles données
                double windSpeed = currentWeather.path("current").path("wind_kph").asDouble();
                String windDir = currentWeather.path("current").path("wind_dir").asText();
                int cloudCover = currentWeather.path("current").path("cloud").asInt();
                double uv = currentWeather.path("current").path("uv").asDouble();
                String localTime = currentWeather.path("location").path("localtime").asText();

                // Mise à jour de l'icône
                String iconUrl = "http:" + weatherIcon;
                Image image = new Image(iconUrl);
                currentWeatherIcon.setImage(image);

                // Création des conteneurs principaux
                VBox mainContainer = new VBox(15);
                mainContainer.setAlignment(Pos.CENTER);

                // En-tête avec ville et heure
                Label cityLabel = new Label(city);
                Label timeLabel = new Label("Mis à jour: " + localTime);
                cityLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");
                timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                // Informations principales
                Label tempLabel = new Label(currentTemp + "°C");
                Label feelsLikeLabel = new Label("Ressenti: " + String.format("%.1f°C", feelsLike));
                Label conditionLabel = new Label(condition);

                tempLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: black;");
                feelsLikeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #333333;");
                conditionLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

                // Création des détails en colonnes
                GridPane detailsGrid = new GridPane();
                detailsGrid.setHgap(20);
                detailsGrid.setVgap(10);
                detailsGrid.setAlignment(Pos.CENTER);

                // Première colonne
                addDetailRow(detailsGrid, 0, 0, "Humidité", humidity + "%");
                addDetailRow(detailsGrid, 1, 0, "Pression", pressure + " hPa");
                addDetailRow(detailsGrid, 2, 0, "Précipitations", String.format("%.1f mm", rainChance));

                // Deuxième colonne
                addDetailRow(detailsGrid, 0, 1, "Vent", String.format("%.1f km/h %s", windSpeed, windDir));
                addDetailRow(detailsGrid, 1, 1, "Couverture nuageuse", cloudCover + "%");
                addDetailRow(detailsGrid, 2, 1, "Index UV", String.format("%.1f", uv));

                // Assemblage final
                VBox headerBox = new VBox(5, cityLabel, timeLabel);
                headerBox.setAlignment(Pos.CENTER);

                VBox weatherBox = new VBox(10, currentWeatherIcon, tempLabel, feelsLikeLabel, conditionLabel);
                weatherBox.setAlignment(Pos.CENTER);

                mainContainer.getChildren().addAll(headerBox, weatherBox, detailsGrid);

                // Mise à jour de l'interface
                currentWeatherBox.getChildren().clear();
                currentWeatherBox.getChildren().add(mainContainer);

                // Mise à jour du fond
                updateBackgroundBasedOnWeather(condition);

            } else {
                showError("Impossible de récupérer la météo pour : " + city);
            }
        } catch (Exception e) {
            showError("Erreur lors de l'affichage de la météo.");
            e.printStackTrace();
        }
    }
    private void addDetailRow(GridPane grid, int row, int col, String label, String value) {
        VBox detailBox = new VBox(5);
        Label titleLabel = new Label(label);
        Label valueLabel = new Label(value);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;"); // Gris foncé pour le titre
        valueLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black; -fx-font-weight: bold;"); // Noir pour la valeur
        detailBox.getChildren().addAll(titleLabel, valueLabel);
        detailBox.setAlignment(Pos.CENTER);
        grid.add(detailBox, col, row);
    }
    public void updateBackgroundBasedOnWeather(String condition) {
        String imagePath;

        // Déterminer l'image en fonction des conditions météorologiques
        switch (condition.toLowerCase()) {
            case "clear":
                imagePath = "/images/ciel.jpg";
                break;
            case "rain":
            case "shower rain":
            case "light rain":
                imagePath = "/images/rainy.png";
                break;
            case "thunderstorm":
                imagePath = "/images/Tonnerre_Clair.jpg";
                break;
            case "snow":
                imagePath = "/images/snow.jpg";
                break;
            case "cloudy":
            case "overcast":
                imagePath = "/images/overcast.jpg";
                break;
            default:
                imagePath = "/images/default.jpg";
                break;
        }
        try {
            String resolvedPath = getClass().getResource(imagePath).toExternalForm();
            currentWeatherBox.setStyle("-fx-background-image: url('" + resolvedPath + "'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center;");
        } catch (NullPointerException e) {
            // Image par défaut si le fichier est introuvable
            currentWeatherBox.setStyle("-fx-background-image: url('/images/default.jpg'); " +
                    "-fx-background-size: cover; " +
                    "-fx-background-position: center;");
        }
    }
    public void displayWeatherHourly(String city) {
        try {
            JsonNode hourlyData = weatherApi.getHourlyWeatherForecastByCity(city);

            if (hourlyData == null || !hourlyData.isArray()) {
                showError("Impossible de récupérer les données horaires.");
                return;
            }

            // Clear previous data
            hourlyForecastBox.getChildren().clear();
            hourlyForecastBox.setSpacing(15);
            hourlyForecastBox.setPadding(new Insets(10));

            // Process each hourly forecast data
            for (JsonNode hourData : hourlyData) {
                String time = hourData.path("time").asText();
                int temperature = hourData.path("temp_c").asInt();
                String condition = hourData.path("condition").path("text").asText();
                String iconUrl = hourData.path("condition").path("icon").asText();

                // Format time
                String formattedTime = time.substring(time.indexOf(" ") + 1);

                // Create hour forecast card
                VBox hourCard = createHourForecastCard(formattedTime, temperature, condition, iconUrl);
                hourlyForecastBox.getChildren().add(hourCard);
            }

        } catch (Exception e) {
            showError("Erreur lors de l'affichage des données horaires.");
            e.printStackTrace();
        }
    }

    private VBox createHourForecastCard(String time, int temperature, String condition, String iconUrl) {
        // Create hour forecast card
        VBox hourCard = new VBox(5);
        hourCard.setSpacing(5);
        hourCard.setPadding(new Insets(5));
        hourCard.setStyle("-fx-border-color: #ccc; -fx-background-color: #fff; -fx-border-radius: 5;");

        // Create hour forecast content
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        ImageView iconView = new ImageView(new Image("http:" + iconUrl));
        iconView.setFitWidth(30);
        iconView.setFitHeight(30);

        Label tempLabel = new Label(temperature + "°C");
        tempLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff4500;");

        Label conditionLabel = new Label(condition);
        conditionLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        // Add hour forecast content to hour card
        hourCard.getChildren().addAll(timeLabel, iconView, tempLabel, conditionLabel);

        return hourCard;
    }

    @FXML
    private void handleProfileButton() {
        try {
            ProfilInterface profilInterface = new ProfilInterface();
            Stage stage = (Stage) currentWeatherBox.getScene().getWindow();
            profilInterface.start(stage);
        } catch (Exception e) {
            logger.severe("Erreur lors de l'ouverture de l'interface profil: " + e.getMessage());
            showError("Impossible d'ouvrir l'interface profil");
        }
    }
    @FXML

    private VBox favoritesCitiesBox;
    private UserPreferences userPreferences;
    private UserPreferencesManager preferencesManager;
    public DisplayGraphManager displayGraphManager;

    public void displayWeatherDetailsForCity(String city) {
        logger.info("Affichage des détails météo pour la ville : " + city);
        displayCurrentWeather(city); // Affiche la météo actuelle
        displayWeatherForecast(city); // Affiche les prévisions météo
        displayWeatherHourly(city);
        //displayGraphManager.displayWeatherGraphs(city);

    }



    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private TitledPane hourlyForecastPane;
    @FXML
    private TitledPane fiveDayForecastPane;

    @FXML
    public void onMenuButtonClick(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            String buttonText = "";

            // Récupérer le texte du bouton depuis le nœud text
            if (clickedButton.getGraphic() instanceof HBox) {
                HBox hbox = (HBox) clickedButton.getGraphic();
                for (Node node : hbox.getChildren()) {
                    if (node instanceof Text) {
                        buttonText = ((Text) node).getText();
                        break;
                    }
                }
            } else {
                // Essayer de récupérer le texte directement
                buttonText = clickedButton.getText();
                if (buttonText == null || buttonText.isEmpty()) {
                    // Chercher dans les propriétés du bouton
                    Object textObj = clickedButton.getProperties().get("text");
                    if (textObj != null) {
                        buttonText = textObj.toString();
                    }
                }
            }

            // Traiter l'action selon le texte du bouton
            switch (buttonText) {
                case "Actuel":
                    mainScrollPane.setVvalue(0.0);
                    break;
                case "Horaire":
                    if (hourlyForecastPane != null) {
                        hourlyForecastPane.setExpanded(true);
                    }
                    mainScrollPane.setVvalue(0.33);
                    break;
                case "Détails":
                    mainScrollPane.setVvalue(0.66);
                    break;
                case "Mensuellement":
                    if (fiveDayForecastPane != null) {
                        fiveDayForecastPane.setExpanded(true);
                    }
                    mainScrollPane.setVvalue(1.0);
                    break;
                default:

                    System.out.println("Bouton non reconnu: " + buttonText);
                    break;
            }
        }
    }
    @FXML
    private GridPane monthlyForecastGrid;
    private void displayMonthlyForecast(String city) {
        try {
            monthlyForecastGrid.getChildren().clear();
            monthlyForecastGrid.setHgap(2);
            monthlyForecastGrid.setVgap(2);

            // Barre des mois
            HBox monthsBar = new HBox(5);
            monthsBar.setAlignment(Pos.CENTER);

            // Obtenir la liste des 12 mois dynamiques à partir du mois actuel
            LocalDate currentDate = LocalDate.now();
            int currentMonthIndex = currentDate.getMonthValue() - 1;
            String[] allMonths = {"Jan", "Fév", "Mars", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"};

            List<String> dynamicMonths = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                dynamicMonths.add(allMonths[(currentMonthIndex + i) % 12]);
            }

            for (int i = 0; i < dynamicMonths.size(); i++) {
                String month = dynamicMonths.get(i);
                Button monthBtn = new Button(month);
                monthBtn.getStyleClass().add("month-button");

                // Ajouter une classe spéciale pour le mois actuel
                if (i == 0) {
                    monthBtn.getStyleClass().add("current-month");
                }

                // Ajouter l'action pour charger les données du mois correspondant
                final int monthOffset = i;
                monthBtn.setOnAction(e -> loadMonthlyData(city, monthOffset));
                monthsBar.getChildren().add(monthBtn);
            }


            monthlyForecastGrid.add(monthsBar, 0, 0, 7, 1);


            String[] daysOfWeek = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
            for (int i = 0; i < 7; i++) {
                Label dayLabel = new Label(daysOfWeek[i]);
                dayLabel.getStyleClass().add("day-header");
                monthlyForecastGrid.add(dayLabel, i, 1);
            }

            // Charger les données pour le mois actuel
            loadMonthlyData(city, 0);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'affichage des prévisions mensuelles");
        }
    }

    private void loadMonthlyData(String city, int monthOffset) {
        try {
            monthlyForecastGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 1);

            // Calculer le mois et l'année à partir du décalage
            LocalDate currentDate = LocalDate.now().plusMonths(monthOffset);
            YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
            int daysInMonth = yearMonth.lengthOfMonth();

            // Obtenir les données météo
            JsonNode monthlyData = weatherApi.getMonthlyWeatherForecastByCity(city);
            JsonNode forecastDays = monthlyData.path("forecast").path("forecastday");

            // Remplir le calendrier
            int row = 2;
            int col = 0;

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);

                VBox dayCell = new VBox(5);
                dayCell.getStyleClass().add("day-cell");
                dayCell.setAlignment(Pos.CENTER);

                // Date
                Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));

                // Icône météo et températures (simulées ici)
                String iconUrl = "https://path-to-icon.com/icon.png"; // Remplacez par l'icône réelle
                ImageView weatherIcon = new ImageView(new Image(iconUrl));
                weatherIcon.setFitWidth(40);
                weatherIcon.setFitHeight(40);

                double maxTemp = Math.random() * 10 + 20; // Simuler une température max
                double minTemp = Math.random() * 10 + 10; // Simuler une température min

                VBox tempBox = new VBox(2);
                tempBox.setAlignment(Pos.CENTER);
                Label maxTempLabel = new Label(String.format("%.0f°", maxTemp));
                Label minTempLabel = new Label(String.format("%.0f°", minTemp));
                maxTempLabel.getStyleClass().add("max-temp");
                minTempLabel.getStyleClass().add("min-temp");
                tempBox.getChildren().addAll(maxTempLabel, minTempLabel);

                // Ajouter une classe pour le jour actuel
                if (date.equals(LocalDate.now())) {
                    dayCell.getStyleClass().add("current-day");
                }

                dayCell.getChildren().addAll(dateLabel, weatherIcon, tempBox);
                monthlyForecastGrid.add(dayCell, col, row);

                col++;
                if (col > 6) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des données pour le mois.");
        }
    }
    @FXML
    private Pane temperatureChartContainer;
    @FXML
    private Pane rainChartContainer;
    @FXML
    private Pane windChartContainer;
    private NotificationService notificationService;
    @FXML
    private TextField citySearchField;

    @FXML
    public void initialize() {
        try {
            System.out.println("Initialisation des conteneurs de graphiques");

            // Vérifier si les conteneurs sont bien injectés
            System.out.println("temperatureChartContainer: " + (temperatureChartContainer != null));
            System.out.println("rainChartContainer: " + (rainChartContainer != null));
            System.out.println("windChartContainer: " + (windChartContainer != null));

            // Initialiser le DisplayGraphManager
            displayGraphManager = new DisplayGraphManager(
                    temperatureChartContainer,
                    rainChartContainer,
                    windChartContainer,
                    userId
            );


        } catch (Exception e) {
            System.err.println("Erreur dans initialize: " + e.getMessage());
            e.printStackTrace();
        }


        try {
            Thread.sleep(5000);
            AlerteService alerteservice = new AlerteService();
            System.out.println("Vérification des alertes météo...");
            alerteservice.checkAndSendWeatherAlert(userId);
            alerteservice.afficherDailyAlerteUtilisateur(userId);

        } catch (Exception e) {
            System.err.println("Erreur d'initialisation: " + e.getMessage());
        }
    }










    private GeolocationApi geolocationApi = new GeolocationApi();
    private boolean locationPermissionGranted;
    private static final String FALLBACK_CITY = "Tokyo";

    public void initializeWithPreferences(boolean locationPermissionGranted, UserPreferencesManager preferencesManager) {
        this.locationPermissionGranted = locationPermissionGranted;
        this.preferencesManager = preferencesManager;

        try {
            String city = null;

            // 1. Essayer d'abord la localisation si autorisée
            if (locationPermissionGranted) {
                logger.info("Localisation autorisée, tentative de récupération...");
                city = geolocationApi.getCurrentCity();
                if (city != null && !city.isEmpty()) {
                    logger.info("Ville trouvée par localisation : " + city);
                    displayWeatherDetailsForCity(city);
                    displayGraphManager.displayWeatherGraphs(city);
                    updateSearchHistory(city);
                    return;
                }
                logger.warning("Échec de la récupération de la ville par localisation");
            }

            // 2. Si la localisation échoue ou n'est pas autorisée, utiliser la ville par défaut
            city = preferencesManager.getDefaultCity(userId);
            if (city != null && !city.isEmpty()) {
                logger.info("Utilisation de la ville par défaut : " + city);
                displayWeatherDetailsForCity(city);
                displayGraphManager.displayWeatherGraphs(city);

                updateSearchHistory(city);
                return;
            }
            logger.info("Aucune ville par défaut trouvée");

            // 3. En dernier recours, utiliser la ville de secours
            handleFallbackCity();

        } catch (Exception e) {
            logger.severe("Erreur lors de l'initialisation : " + e.getMessage());
            handleFallbackCity();
        }
    }

    private void handleFallbackCity() {
        try {
            logger.info("Utilisation de la ville de secours : " + FALLBACK_CITY);
            displayWeatherDetailsForCity(FALLBACK_CITY);
            displayGraphManager.displayWeatherGraphs(FALLBACK_CITY);

            updateSearchHistory(FALLBACK_CITY);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Utilisation temporaire de " + FALLBACK_CITY + " comme ville de secours.");
                alert.showAndWait();
            });
        } catch (Exception e) {
            logger.severe("Erreur critique lors de l'utilisation de la ville de secours : " + e.getMessage());
        }
    }

    private void updateSearchHistory(String city) {
        try {
            databaseManager.updateSearchHistory(userId, city);
            logger.info("Historique de recherche mis à jour pour la ville : " + city);
        } catch (SQLException e) {
            logger.warning("Erreur lors de la mise à jour de l'historique : " + e.getMessage());
        }
    }



    private void loadFavoriteCities() {
        try {
            favoritesCitiesBox.getChildren().clear();
            List<String> favoriteCities = userPreferences.getFavoriteCities(userId);

            logger.info("Chargement des villes favorites pour l'utilisateur " + userId);

            // Ajouter les villes existantes
            for (int i = 0; i < favoriteCities.size() && i < 5; i++) {
                String city = favoriteCities.get(i);
                if (city != null && !city.trim().isEmpty()) {
                    HBox cityItem = createCityItem(city, i + 1);
                    favoritesCitiesBox.getChildren().add(cityItem);
                }
            }

            // Ajouter le bouton d'ajout si moins de 5 villes
            if (favoriteCities.size() < 5) {
                Button addButton = new Button("+ Ajouter une ville");
                addButton.getStyleClass().add("add-city-button");
                addButton.setOnAction(e -> handleAddFavoriteCity());
                favoritesCitiesBox.getChildren().add(addButton);
            }
        } catch (Exception e) {
            logger.severe("Erreur lors du chargement des villes favorites: " + e.getMessage());
            showError("Erreur lors du chargement des villes favorites");
        }
    }

    private HBox createCityItem(String city, int index) {
        HBox cityItem = new HBox(10);
        cityItem.setAlignment(Pos.CENTER_LEFT);
        cityItem.getStyleClass().add("favorite-city-item");

        // Label de la ville cliquable
        Button cityButton = new Button(city);
        cityButton.getStyleClass().add("favorite-city-button");
        cityButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cityButton, Priority.ALWAYS);

        // Ajouter l'action pour afficher la météo
        cityButton.setOnAction(e -> {
            searchCityTextField.setText(city);
            displayWeatherDetailsForCity(city);
            displayGraphManager.displayWeatherGraphs(city);

        });

        // Bouton modifier
        Button editButton = new Button("✎");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> handleEditFavoriteCity(city, index));

        // Bouton supprimer
        Button removeButton = new Button("✖");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> handleRemoveFavoriteCity(index));

        cityItem.getChildren().addAll(cityButton, editButton, removeButton);
        return cityItem;
    }
    private void handleAddFavoriteCity() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Ajouter une ville");
        dialog.setHeaderText("Ajouter une nouvelle ville favorite");
        dialog.setContentText("Nom de la ville:");

        dialog.showAndWait().ifPresent(city -> {
            if (!city.isEmpty()) {
                try {
                    if (userPreferences.addFavoriteCity(userId, city)) {
                        loadFavoriteCities(); // Recharger la liste
                        logger.info("Ville ajoutée avec succès: " + city);
                    } else {
                        showError("Erreur lors de l'ajout de la ville");
                    }
                } catch (Exception e) {
                    logger.severe("Erreur lors de l'ajout: " + e.getMessage());
                    showError("Erreur lors de l'ajout de la ville");
                }
            }
        });
    }

    private void handleEditFavoriteCity(String currentCity, int index) {
        TextInputDialog dialog = new TextInputDialog(currentCity);
        dialog.setTitle("Modifier la ville");
        dialog.setHeaderText("Modifier la ville favorite");
        dialog.setContentText("Nouvelle ville:");

        dialog.showAndWait().ifPresent(newCity -> {
            if (!newCity.isEmpty()) {
                try {
                    if (userPreferences.editFavoriteCity(index, newCity)) {
                        loadFavoriteCities(); // Recharger la liste
                        logger.info("Ville modifiée avec succès");
                    } else {
                        showError("Erreur lors de la modification de la ville");
                    }
                } catch (Exception e) {
                    logger.severe("Erreur lors de la modification: " + e.getMessage());
                    showError("Erreur lors de la modification de la ville");
                }
            }
        });
    }

    private void handleRemoveFavoriteCity(int index) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer la ville");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cette ville ?");
        confirmation.setContentText("Cette action ne peut pas être annulée.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (userPreferences.removeFavoriteCity(index)) {
                        loadFavoriteCities(); // Recharger la liste
                        logger.info("Ville supprimée avec succès");
                    } else {
                        showError("Erreur lors de la suppression de la ville");
                    }
                } catch (Exception e) {
                    logger.severe("Erreur lors de la suppression: " + e.getMessage());
                    showError("Erreur lors de la suppression de la ville");
                }
            }
        });
    }
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}



