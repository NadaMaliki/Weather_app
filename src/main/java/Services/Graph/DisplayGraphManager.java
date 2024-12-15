package Services.Graph;


import Dao.DBConnexion;
import Dao.GraphManager;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;

public class DisplayGraphManager {
    private Pane temperatureChartContainer;
    private Pane rainChartContainer;
    private Pane windChartContainer;
    private DBConnexion dbConnection;
    private int userId;

    // Constructeur
    public DisplayGraphManager(Pane tempContainer, Pane rainContainer, Pane windContainer, int userId) {
        this.temperatureChartContainer = tempContainer;
        this.rainChartContainer = rainContainer;
        this.windChartContainer = windContainer;
        this.userId = userId;
        try {
            this.dbConnection = new DBConnexion();
        } catch (Exception e) {
            showError("Erreur de connexion à la base de données : " + e.getMessage());
        }
    }

    public void displayWeatherGraphs(String city) {
        try {
            GraphManager graphManager = new GraphManager(dbConnection);
            WeatherGraphService graphService = new WeatherGraphService(graphManager);

            // Générer les graphiques
            graphService.generateGraphsForCity(city, userId);

            // Afficher les graphiques
            displayStoredGraphs(city, graphManager);

        } catch (Exception e) {
            showError("Erreur lors de l'affichage des graphiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayStoredGraphs(String city, GraphManager graphManager) {
        try {
            // Température
            byte[] temperatureGraphData = graphManager.getGraphImage(userId, city, "Temperature");
            if (temperatureGraphData != null) {
                displayGraphImage(temperatureGraphData, temperatureChartContainer);
            }

            // Pluie
            byte[] rainGraphData = graphManager.getGraphImage(userId, city, "Rainfall");
            if (rainGraphData != null) {
                displayGraphImage(rainGraphData, rainChartContainer);
            }

            // Vent
            byte[] windGraphData = graphManager.getGraphImage(userId, city, "Wind");
            if (windGraphData != null) {
                displayGraphImage(windGraphData, windChartContainer);
            }

        } catch (SQLException e) {
            showError("Erreur lors de la récupération des graphiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayGraphImage(byte[] imageData, Pane container) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            Image image = new Image(bis);
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(container.getWidth());
            imageView.setFitHeight(container.getHeight());
            imageView.setPreserveRatio(true);

            container.getChildren().clear();
            container.getChildren().add(imageView);
        } catch (Exception e) {
            showError("Erreur lors de l'affichage du graphique : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}