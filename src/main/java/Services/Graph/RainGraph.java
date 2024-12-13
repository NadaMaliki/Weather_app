package Services.Graph;

import Dao.GraphManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.fasterxml.jackson.databind.JsonNode;

public class RainGraph {
	
	// Generate and save the rainfall graph
    public void generateRainGraph(JsonNode forecastData, String cityName, int userId, GraphManager graphManager) {
        try {
            // Prepare the dataset
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            JsonNode forecastDays = forecastData.get("forecast").get("forecastday");
            for (JsonNode day : forecastDays) {
                String date = day.get("date").asText();
                double rainAmount = day.get("day").get("totalprecip_mm").asDouble(); // Rainfall in mm
                dataset.addValue(rainAmount, "Rainfall (mm)", date);
            }

            // Create the chart
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Rainfall Forecast for " + cityName,
                    "Date",
                    "Rainfall (mm)",
                    dataset
            );

            // Save as a temporary image file
            File tempFile = File.createTempFile("/rain_graph_.png", "a");
            ChartUtils.saveChartAsPNG(tempFile, barChart, 800, 400);

            // Read the file as byte array
            byte[] imageData = Files.readAllBytes(tempFile.toPath());

            // Insert or update the graph in the database
            boolean b= graphManager.upsertGraph(userId, cityName, "Rainfall", imageData);
            System.out.print(b);



            // Delete the temporary file
            tempFile.delete();

            System.out.println("Rainfall graph saved for city: " + cityName);
        } catch (IOException | SQLException e) {
            System.err.println("Error generating or saving rainfall graph: " + e.getMessage());
        }
    }

}
