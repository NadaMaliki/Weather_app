package Apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApi {

    public String apiKey = "1c69d1bd05d947c5aa8115353241811";  // la clé de l'api
    public String baseUrl = "http://api.weatherapi.com/v1/current.json";  // l'url de base

    public JsonNode getCurrentWeatherByCity(String ville) {       // cette méthode va prendre une ville et va retourner la situation méteorologique courante sous format JSON       
        String endpoint = this.baseUrl + "?key=" + this.apiKey + "&q=" + ville + "&aqi=no";    // c'est l'URL pour obtenir (current weather infos)
        return fetchWeatherData(endpoint);    // cette méthode va lancer la requete HTTP et va retourner l'objet courant sous format JSON .
    }

    public JsonNode getWeatherForecastByCity(String ville) {   // cette méthode va prendre une ville et va retourner la prévision des 5 jours prochaines de la situation météorologique
        String endpoint = this.baseUrl.replace("current.json", "forecast.json") + "?key=" + this.apiKey + "&q=" + ville + "&days=5&aqi=no";  // ici on change current.json (qui représente l'api de situation courante) par forecast.json(qui représente l'api de la prévision météorologique)
        return fetchWeatherData(endpoint);    // cette méthode va lancer la requete HTTP et va retourner l'objet courant sous format JSON . 
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

    public JsonNode fetchWeatherData(String endpoint) {  // cette méthode est responsable de lancer la requete HTTP .
        try {
            URL url = new URL(endpoint);  // on initialise un URL 
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // on ouvre la connection
            connection.setRequestMethod("GET"); // on spécifie la méthode d'envoie
            connection.setConnectTimeout(10000); // si la connection dépasse une limite de temps , elle va etre rejetée.
            connection.setReadTimeout(10000); // si la lecture dépasse une limite de temps , elle va etre rejetée.

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // initialisation de buffer
            StringBuilder response = new StringBuilder();  // initialisation d'un buffer qui va contenir la réponse . 
            String line; // une variable qui va stocker les lignes de la réponse .
            while ((line = reader.readLine()) != null) {     // a while loop to store responses line by line and store it into line variable .
                response.append(line);
            }
            reader.close();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.toString());     // objectMapper s'occupe de convertir tout en JSON.

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        WeatherApi weatherApi = new WeatherApi();

//        JsonNode currentWeather = weatherApi.getCurrentWeatherByCity("Marrakech"); // test sur la situation courante de Marrakech
//        System.out.println("Current Weather: " + currentWeather.toString());

        JsonNode forecastWeather = weatherApi.getWeatherForecastByCity("Marrakech");  // test sur le forecast de 5 jours de Marrakech
        System.out.println("Weather Forecast: " + forecastWeather.toString());
        
//        // Test: Current weather by coordinates
//        JsonNode currentWeatherCoords = weatherApi.getCurrentWeatherByCoordinates(31.63, -8.0);
//        System.out.println("Current Weather (Coords): " + currentWeatherCoords);
    }


}