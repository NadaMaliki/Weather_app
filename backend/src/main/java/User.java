import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Apis.CityValidationApi;
import Apis.GeolocationApi;
import Apis.WeatherApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class User extends Visitor {
	private String username;
    private String email;
//    private UserPreferences preferences;
    private boolean alertEnabled;
//    private List<String> searchHistory;
    private boolean isLoggedIn;    
    
    
    
    @Override
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
        
     // Vérifier si l'API a renvoyé une réponse valide
        if (forecastWeather == null || !forecastWeather.has("forecast")) {
            System.out.println("La ville '" + city + "' n'existe pas ou les données météo ne sont pas disponibles.");
            return forecastList;
        }

        try {
            JsonNode forecastDays = forecastWeather.get("forecast").get("forecastday");

            // Parcourir chaque jour
            for (JsonNode day : forecastDays) {
                String date = day.get("date").asText();
                double tempMax = day.get("day").get("maxtemp_c").asDouble();
                double tempMin = day.get("day").get("mintemp_c").asDouble();
                double precipitation = day.get("day").get("daily_chance_of_rain").asDouble();
                String description = day.get("day").get("condition").get("text").asText();
                double humidity = day.get("day").get("avghumidity").asDouble();
                double windSpeed = day.get("day").get("maxwind_kph").asDouble();

                // Prévisions horaires
                List<String> hourlyForecasts = new ArrayList<>();
                JsonNode hourlyData = day.get("hour");
                for (JsonNode hour : hourlyData) {
                    String hourTime = hour.get("time").asText();
                    String hourCondition = hour.get("condition").get("text").asText();
                    double hourTemp = hour.get("temp_c").asDouble();
                    double hourRain = hour.get("chance_of_rain").asDouble();
                    hourlyForecasts.add(hourTime + ": " + hourCondition + ", " + hourTemp + "°C, Rain: " + hourRain + "%");
                }

                Forecast forecast = new Forecast(date, tempMax, tempMin, precipitation, description, humidity, windSpeed, hourlyForecasts);
                forecastList.add(forecast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return forecastList;
    }
    
    @Override
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
            for (JsonNode day : forecastDays) {
                String date = day.get("date").asText();
                double tempMax = day.get("day").get("maxtemp_c").asDouble();
                double tempMin = day.get("day").get("mintemp_c").asDouble();
                double precipitation = day.get("day").get("daily_chance_of_rain").asDouble();
                String description = day.get("day").get("condition").get("text").asText();
                double humidity = day.get("day").get("avghumidity").asDouble();
                double windSpeed = day.get("day").get("maxwind_kph").asDouble();

                // Prévisions horaires
                List<String> hourlyForecasts = new ArrayList<>();
                JsonNode hourlyData = day.get("hour");
                for (JsonNode hour : hourlyData) {
                    String hourTime = hour.get("time").asText();
                    String hourCondition = hour.get("condition").get("text").asText();
                    double hourTemp = hour.get("temp_c").asDouble();
                    double hourRain = hour.get("chance_of_rain").asDouble();
                    hourlyForecasts.add(hourTime + ": " + hourCondition + ", " + hourTemp + "°C, Rain: " + hourRain + "%");
                }

                // Ajouter les prévisions à la liste
                Forecast forecast = new Forecast(date, tempMax, tempMin, precipitation, description, humidity, windSpeed, hourlyForecasts);
                forecastList.add(forecast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return forecastList;
    }


    
    public static void main(String[] args) {
    	
    	User user = new User();
//        List<Forecast> userForecasts = user.searchWeather("Marrakech");
//        System.out.println("\nPrévisions détaillées pour l'utilisateur :");
//        for (Forecast forecast : userForecasts) {
//            System.out.println("Date: " + forecast.getDate());
//            System.out.println("Temp Max: " + forecast.getTemperatureMax() + "°C");
//            System.out.println("Temp Min: " + forecast.getTemperatureMin() + "°C");
//            System.out.println("Chance de pluie: " + forecast.getPrecipitationProbability() + "%");
//            System.out.println("Description: " + forecast.getDescription());
//            System.out.println("Humidité: " + forecast.getHumidity() + "%");
//            System.out.println("Vent: " + forecast.getWindSpeed() + " km/h");
//            
//            System.out.println("Prévisions horaires :");
//            for (String hourly : forecast.getHourlyForecasts()) {
//                System.out.println(hourly);
//            }
//            System.out.println("---------------------------");
//        }
        
     // Obtenir et afficher les prévisions météo basées sur la géolocalisation
        System.out.println("Prévisions météo basées sur la géolocalisation :");
        List<Forecast> forecasts = user.getWeatherBasedOnLocation();
        for (Forecast forecast : forecasts) {
            System.out.println("Date: " + forecast.getDate());
            System.out.println("Temp Max: " + forecast.getTemperatureMax() + "°C");
            System.out.println("Temp Min: " + forecast.getTemperatureMin() + "°C");
            System.out.println("Chance de pluie: " + forecast.getPrecipitationProbability() + "%");
            System.out.println("Description: " + forecast.getDescription());
            System.out.println("Humidité: " + forecast.getHumidity() + "%");
            System.out.println("Vent: " + forecast.getWindSpeed() + " km/h");
            System.out.println("Prévisions horaires :");
            for (String hourly : forecast.getHourlyForecasts()) {
                System.out.println(hourly);
            }
            System.out.println("---------------------------");
        }
        user.fetchWeather(false, "london");

    }
	
}

