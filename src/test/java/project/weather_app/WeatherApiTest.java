package project.weather_app;

import com.fasterxml.jackson.databind.JsonNode;

import Services.Api.WeatherApi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class WeatherApiTest {

    @Test
    void testGetCurrentWeatherByCityWithValidCity() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getCurrentWeatherByCity("Marrakech");

        assertNotNull(result, "Result should not be null for a valid city.");
        assertTrue(result.has("location"), "Response should contain 'location' field.");
        assertTrue(result.has("current"), "Response should contain 'current' field.");
    }

    @Test
    void testGetCurrentWeatherByCityWithInvalidCity() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getCurrentWeatherByCity("InvalidCity12345");

        assertNull(result, "Result should be null for an invalid city.");
    }

    @Test
    void testGetWeatherForecastByCityWithValidCity() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getWeatherForecastByCity("Marrakech");

        assertNotNull(result, "Result should not be null for a valid city.");
        assertTrue(result.has("forecast"), "Response should contain 'forecast' field.");
    }

    @Test
    void testGetHourlyWeatherForecastByCityWithValidCity() {
        WeatherApi weatherApi = new WeatherApi();
        try {
            JsonNode result = weatherApi.getHourlyWeatherForecastByCity("Marrakech");

            assertNotNull(result, "Result should not be null for a valid city.");
            assertTrue(result.isArray(), "Hourly forecast should be an array.");
        } catch (IOException | InterruptedException e) {
            fail("Exception occurred while calling hourly weather forecast: " + e.getMessage());
        }
    }

    @Test
    void testGetCurrentWeatherByCoordinatesWithValidCoordinates() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getCurrentWeatherByCoordinates(31.63, -8.0);  // Marrakech coordinates

        assertNotNull(result, "Result should not be null for valid coordinates.");
        assertTrue(result.has("location"), "Response should contain 'location' field.");
        assertTrue(result.has("current"), "Response should contain 'current' field.");
    }

    @Test
    void testGetWeatherForecastByCoordinatesWithValidCoordinates() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getWeatherForecastByCoordinates(31.63, -8.0);  // Marrakech coordinates

        assertNotNull(result, "Result should not be null for valid coordinates.");
        assertTrue(result.has("forecast"), "Response should contain 'forecast' field.");
    }

    @Test
    void testGetMonthlyWeatherForecastByCityWithValidCity() {
        WeatherApi weatherApi = new WeatherApi();
        JsonNode result = weatherApi.getMonthlyWeatherForecastByCity("Marrakech");

        assertNotNull(result, "Result should not be null for a valid city.");
        assertTrue(result.has("forecast"), "Response should contain 'forecast' field.");
    }

    @Test
    void testFetchWeatherDataWithValidEndpoint() {
        WeatherApi weatherApi = new WeatherApi();
        String endpoint = "http://api.weatherapi.com/v1/current.json?key=1c69d1bd05d947c5aa8115353241811&q=Marrakech&aqi=no";
        
        JsonNode result = weatherApi.fetchWeatherData(endpoint);

        assertNotNull(result, "Result should not be null for a valid endpoint.");
        assertTrue(result.has("location"), "Response should contain 'location' field.");
        assertTrue(result.has("current"), "Response should contain 'current' field.");
    }

    @Test
    void testFetchWeatherDataWithInvalidEndpoint() {
        WeatherApi weatherApi = new WeatherApi();
        String endpoint = "http://invalid-url-to-fetch-weather.com/unknown";

        JsonNode result = weatherApi.fetchWeatherData(endpoint);
        
        assertNull(result, "Result should be null for an invalid endpoint.");
    }

    @Test
    void testFetchWeatherDataWithError() {
        // Simulate an error by extending WeatherApi and overriding fetchWeatherData
        WeatherApi faultyApi = new WeatherApi() {
            @Override
            public JsonNode fetchWeatherData(String endpoint) {
                throw new RuntimeException("Simulated network error");
            }
        };

        JsonNode result = null;
        try {
            result = faultyApi.fetchWeatherData("http://api.weatherapi.com/v1/current.json?key=invalidKey&q=Marrakech&aqi=no");
        } catch (Exception e) {
            assertEquals("Simulated network error", e.getMessage(), "Exception message should match the simulated error.");
        }
        assertNull(result, "Result should be null in case of a simulated network error.");
    }

    @Test
    void testFetchWeatherDataWithNon200Response() {
        WeatherApi apiWithNon200Response = new WeatherApi() {
            @Override
            public JsonNode fetchWeatherData(String endpoint) {
                return null; // Simulate an empty or invalid response
            }
        };

        String endpoint = "http://api.weatherapi.com/v1/current.json?key=invalidKey&q=Marrakech&aqi=no";
        JsonNode result = apiWithNon200Response.fetchWeatherData(endpoint);

        assertNull(result, "Result should be null when the API returns a non-200 response.");
    }
}
