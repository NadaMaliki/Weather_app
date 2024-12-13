package Services.Graph;

import com.fasterxml.jackson.databind.JsonNode;

import Dao.GraphManager;
import Services.Api.WeatherApi;

public class WeatherGraphService {
	
	 private WeatherApi weatherApi;
	    private GraphManager graphManager;

	    // Initialize graph classes
	    RainGraph rainGraph = new RainGraph();
	    TemperatureGraph temperatureGraph = new TemperatureGraph();
	    WindGraph windGraph = new WindGraph();

	    // Constructor
	    public WeatherGraphService(GraphManager graphManager) {
	        this.weatherApi = new WeatherApi();
	        this.graphManager = graphManager;
	    }

	    // Generate all graphs for a given city and user
	    public void generateGraphsForCity(String cityName, int userId) {
	        try {
	            // Fetch weather forecast data
	            JsonNode forecastData = weatherApi.getWeatherForecastByCity(cityName);

	            if (forecastData == null || forecastData.get("forecast") == null) {
	                System.err.println("Failed to fetch forecast data for city: " + cityName);
	                return;
	            }

	            // Check if updates are needed for each graph type
	            if (graphManager.isUpdateNeeded(userId, cityName, "RainGraph")) {
	                rainGraph.generateRainGraph(forecastData, cityName, userId ,graphManager);
	            }

	            if (graphManager.isUpdateNeeded(userId, cityName, "TemperatureGraph")) {
	                temperatureGraph.generateTemperatureGraph(forecastData, cityName, userId, graphManager);
	            }

	            if (graphManager.isUpdateNeeded(userId, cityName, "WindGraph")) {
	                windGraph.generateWindGraph(forecastData, cityName, userId , graphManager);
	            }

	            System.out.println("All necessary graphs have been updated for city: " + cityName);

	        } catch (Exception e) {
	            System.err.println("Error generating graphs for city: " + cityName);
	            e.printStackTrace();
	        }
	    }

}
