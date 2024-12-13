package Services.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.fasterxml.jackson.databind.JsonNode;

import Dao.GraphManager;

public class WindGraph {
	
	  // Generate and save the wind speed graph
    public void generateWindGraph(JsonNode forecastData, String cityName, int userId, GraphManager graphManager) {
        try {
            // Prepare the dataset
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            JsonNode forecastDays = forecastData.get("forecast").get("forecastday");
            for (JsonNode day : forecastDays) {
                String date = day.get("date").asText();
                double maxWindSpeed = day.get("day").get("maxwind_kph").asDouble(); // Max wind speed in km/h
                dataset.addValue(maxWindSpeed, "Wind Speed (km/h)", date);
            }

            // Create the chart
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Wind Speed Forecast for " + cityName,
                    "Date",
                    "Wind Speed (km/h)",
                    dataset
            );

            // Save as a temporary image file
            File tempFile = File.createTempFile("wind_graph_", ".png");
            ChartUtils.saveChartAsPNG(tempFile, barChart, 800, 400);

            // Read the file as byte array
            byte[] imageData = Files.readAllBytes(tempFile.toPath());

            // Insert or update the graph in the database
            graphManager.upsertGraph(userId, cityName, "Wind", imageData);

            // Delete the temporary file
            tempFile.delete();

            System.out.println("Wind graph saved for city: " + cityName);
        } catch (IOException | SQLException e) {
            System.err.println("Error generating or saving wind graph: " + e.getMessage());
        }
    }

}
