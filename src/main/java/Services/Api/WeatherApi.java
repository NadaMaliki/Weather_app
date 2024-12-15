package Services.Api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherApi {

    private final String apiKey = "1c69d1bd05d947c5aa8115353241811";  // la clé de l'API
    private final String baseUrl = "http://api.weatherapi.com/v1/current.json";  // l'URL de base

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode getCurrentWeatherByCity(String city) {
        String endpoint = this.baseUrl + "?key=" + this.apiKey + "&q=" + city + "&aqi=no";
        return fetchWeatherData(endpoint);
    }


    public JsonNode getWeatherForecastByCity(String city) {
        String endpoint = this.baseUrl.replace("current.json", "forecast.json") + "?key=" + this.apiKey + "&q=" + city + "&days=5&aqi=no";
        return fetchWeatherData(endpoint);
    }

    public JsonNode getHourlyWeatherForecastByCity(String city) throws IOException, InterruptedException {
        String url = String.format("http://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=1&aqi=no", apiKey, city);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode forecastData = mapper.readTree(response.body()).path("forecast").path("forecastday").get(0).path("hour");

        return forecastData;  // Retourne les données horaires pour les 24 heures
    }




    // Get current weather by latitude and longitude
    public JsonNode getCurrentWeatherByCoordinates(double latitude, double longitude) {
        String query = latitude + "," + longitude;
        String endpoint = this.baseUrl + "?key=" + this.apiKey + "&q=" + query + "&aqi=no";
        return fetchWeatherData(endpoint);
    }

    // Get weather forecast by latitude and longitude
    public JsonNode getWeatherForecastByCoordinates(double latitude, double longitude) {
        String query = latitude + "," + longitude;
        String endpoint = this.baseUrl.replace("current.json", "forecast.json")
                + "?key=" + this.apiKey + "&q=" + query + "&days=5&aqi=no";
        return fetchWeatherData(endpoint);
    }

    public JsonNode getMonthlyWeatherForecastByCity(String city) {
        String endpoint = this.baseUrl.replace("current.json", "forecast.json") 
            + "?key=" + this.apiKey 
            + "&q=" + city 
            + "&days=31&aqi=no";
        return fetchWeatherData(endpoint);
    }

    public JsonNode fetchWeatherData(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Error: " + response.statusCode() + " - " + response.body());
                return null;
            }

            return mapper.readTree(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        WeatherApi weatherApi = new WeatherApi();

        // Test Current Weather for Marrakech
        JsonNode currentWeather = weatherApi.getCurrentWeatherByCity("Marrakech");
        System.out.println("Current Weather: " + currentWeather.toString());

        // Test 5-day Forecast for Marrakech
        JsonNode forecastWeather = weatherApi.getWeatherForecastByCity("Marrakech");
        System.out.println("Weather Forecast: " + forecastWeather.toString());

        // Test: Current weather by coordinates
        JsonNode currentWeatherCoords = weatherApi.getCurrentWeatherByCoordinates(31.63, -8.0);
        System.out.println("Current Weather (Coords): " + currentWeatherCoords);
    }
}
