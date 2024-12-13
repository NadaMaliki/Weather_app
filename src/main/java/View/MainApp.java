package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Services.Api.GeolocationApi;
import Controller.MainViewController;

public class MainApp extends Application {
    private static boolean locationPermissionGranted = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Afficher d'abord la boîte de dialogue de permission
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Permission de localisation");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous activer la localisation pour afficher les prévisions météo ?");

            ButtonType yesButton = new ButtonType("Oui");
            ButtonType noButton = new ButtonType("Non");
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                try {
                    // Stocker la réponse de permission
                    locationPermissionGranted = (response == yesButton);
                    System.out.println("Permission de localisation : " + locationPermissionGranted);

                    // Charger le FXML
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                    Parent root = loader.load();

                    // Obtenir le contrôleur
                    MainViewController controller = loader.getController();

                    // Initialiser avec la permission de localisation
                    if (locationPermissionGranted) {
                        try {
                            GeolocationApi geolocationApi = new GeolocationApi();
                            String geoInfo = geolocationApi.getGeolocationInfo();
                            String city = geolocationApi.parseCityFromGeolocationInfo(geoInfo);
                            if (city != null && !city.isEmpty()) {
                                controller.displayWeather(city);
                                controller.displayHourlyWeather(city);
                            } else {
                                controller.displayWeather("Tanger");
                                controller.displayHourlyWeather("Tanger");
                            }
                        } catch (Exception e) {
                            controller.displayWeather("Rouen");
                            controller.displayHourlyWeather("Rouen");
                        }
                    } else {
                        controller.displayWeather("Rouen");
                        controller.displayHourlyWeather("Rouen");
                    }

                    Scene scene = new Scene(root, 600, 400);
                    primaryStage.setTitle("Application Météo");
                    primaryStage.setScene(scene);
                    primaryStage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur lors du chargement de l'application");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du démarrage de l'application");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode publique pour accéder à l'état de la permission
    public static boolean isLocationPermissionGranted() {
        System.out.println("Vérification de la permission : " + locationPermissionGranted);
        return locationPermissionGranted;
    }

    public static void main(String[] args) {
        launch(args);
    }
}