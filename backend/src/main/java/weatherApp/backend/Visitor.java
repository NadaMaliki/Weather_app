package weatherApp.backend;

import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Apis.CityValidationApi;
import Apis.GeolocationApi;
import Apis.WeatherApi;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Visitor {
    private String currentCity;
    private boolean locationEnabled = false;
    private List<Forecast> forecasts;
    
    
    /* 
     * l'utilisateur peut soit avoir les previsions par:
     * 		ville: avec searchWeather (isLocationEnabled == False)
     * 		localisation: avec getWeatherBasedOnLocation (isLocationEnabled == true)
     */
    
    public void fetchWeather(boolean isLocationEnabled, String city) {

        if (isLocationEnabled) {
            System.out.println("Obtention des prévisions météo basées sur la géolocalisation...");
            forecasts = getWeatherBasedOnLocation();
        } else {
            System.out.println("Obtention des prévisions météo pour la ville : " + city);
            forecasts = searchWeather(city);
        }
    }
    
    
    /*
     * Forecast des informations météos par ville
     */
    
    public List<Forecast> searchWeather(String city) {
        List<Forecast> forecastList = new ArrayList<>();
        if (city == null || city.isEmpty()) {
            return forecastList;
        }
        
     // Vérification si la ville est valide avant d'effectuer la recherche
        CityValidationApi cityApi = new CityValidationApi();
        if (!cityApi.isCityValid(city)) {
            System.out.println("La ville '" + city + "' n'est pas valide.");
            return forecastList;
        }

        // Instancier l'API météo
        WeatherApi weatherApi = new WeatherApi();
        JsonNode forecastWeather = weatherApi.getWeatherForecastByCity(city);

        try {
            JsonNode forecastDays = forecastWeather.get("forecast").get("forecastday");

            // Limiter à 3 jours
            int maxDays = Math.min(forecastDays.size(), 3);
            for (int i = 0; i < maxDays; i++) {
                JsonNode day = forecastDays.get(i);
                
                // Extraire les infos limités pour le visiteur
                String date = day.get("date").asText();
                double tempMax = day.get("day").get("maxtemp_c").asDouble();
                double tempMin = day.get("day").get("mintemp_c").asDouble();
                double precipitation = day.get("day").get("daily_chance_of_rain").asDouble();
                String description = day.get("day").get("condition").get("text").asText();
                double humidity = day.get("day").get("avghumidity").asDouble();
                double windSpeed = day.get("day").get("maxwind_kph").asDouble();

                // Ne pas inclure les infos heure par heure
                Forecast forecast = new Forecast(date, tempMax, tempMin, precipitation, description, humidity, windSpeed, null);
                
                // Ajouter l'objet Forecast à la liste
                forecastList.add(forecast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return forecastList;
    }

    
    /*
     * Forecast des informations météos par Localisation
     */
    
    public List<Forecast> getWeatherBasedOnLocation() {
        List<Forecast> forecastList = new ArrayList<>();

        // Instanciation de l'API Geolocation
        GeolocationApi geoApi = new GeolocationApi();

        // Récupérer les informations de géolocalisation
        String geolocationInfo = geoApi.getGeolocationInfo();
        if (geolocationInfo.contains("Unable")) {
            System.out.println("Impossible de récupérer les informations de géolocalisation.");
            return forecastList;
        }

        try {
            // Extraire les coordonnées de la réponse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode geoData = objectMapper.readTree(geolocationInfo);
            double latitude = geoData.get("latitude").asDouble();
            double longitude = geoData.get("longitude").asDouble();

            // Utiliser WeatherApi pour récupérer les prévisions météorologiques
            WeatherApi weatherApi = new WeatherApi();
            JsonNode forecastWeather = weatherApi.getWeatherForecastByCoordinates(latitude, longitude);

            if (forecastWeather == null || !forecastWeather.has("forecast")) {
                System.out.println("Impossible de récupérer les prévisions météo.");
                return forecastList;
            }

            // Parcourir les prévisions et remplir la liste
            JsonNode forecastDays = forecastWeather.get("forecast").get("forecastday");

            // Limiter à 3 jours maximum
            int maxDays = Math.min(forecastDays.size(), 3);
            for (int i = 0; i < maxDays; i++) {
                JsonNode day = forecastDays.get(i);

                // Extraire les informations principales
                String date = day.get("date").asText();
                double tempMax = day.get("day").get("maxtemp_c").asDouble();
                double tempMin = day.get("day").get("mintemp_c").asDouble();
                double precipitation = day.get("day").get("daily_chance_of_rain").asDouble();
                String description = day.get("day").get("condition").get("text").asText();
                double humidity = day.get("day").get("avghumidity").asDouble();
                double windSpeed = day.get("day").get("maxwind_kph").asDouble();

                // Ne pas inclure les informations horaires
                Forecast forecast = new Forecast(date, tempMax, tempMin, precipitation, description, humidity, windSpeed, null);

                // Ajouter à la liste
                forecastList.add(forecast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return forecastList;
    }
    
    
    

    
    public static void main(String[] args) {
        // Visiteur
        Visitor visitor = new Visitor();
//        List<Forecast> visitorForecasts = visitor.searchWeather("london");
//        System.out.println("Prévisions pour le visiteur (3 jours) :");
//        for (Forecast forecast : visitorForecasts) {
//            System.out.println("Date: " + forecast.getDate());
//            System.out.println("Temp Max: " + forecast.getTemperatureMax() + "°C");
//            System.out.println("Temp Min: " + forecast.getTemperatureMin() + "°C");
//            System.out.println("Chance de pluie: " + forecast.getPrecipitationProbability() + "%");
//            System.out.println("Description: " + forecast.getDescription());
//            System.out.println("Humidité: " + forecast.getHumidity() + "%");
//            System.out.println("Vent: " + forecast.getWindSpeed() + " km/h");
//            System.out.println("---------------------------");
//        }
        
     // Tester les prévisions météo basées sur la géolocalisation
        List<Forecast> forecasts = visitor.getWeatherBasedOnLocation();
        System.out.println("Prévisions météo basées sur la géolocalisation :");
        for (Forecast forecast : forecasts) {
            System.out.println("Date: " + forecast.getDate());
            System.out.println("Température Max: " + forecast.getTemperatureMax() + "°C");
            System.out.println("Température Min: " + forecast.getTemperatureMin() + "°C");
            System.out.println("Chance de Pluie: " + forecast.getPrecipitationProbability() + "%");
            System.out.println("Description: " + forecast.getDescription());
            System.out.println("Humidité: " + forecast.getHumidity() + "%");
            System.out.println("Vent: " + forecast.getWindSpeed() + " km/h");
            System.out.println("-----------------------------");
        }
    }
}
