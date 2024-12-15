package project.weather_app;

import Services.Api.CityValidationApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class CityValidationApiTest {

    private final CityValidationApi cityValidationApi = new CityValidationApi();

    @Test
    void testIsCityValid_ValidCity() {
       
        String cityName = "Marrakech";  // Example of a valid city
        boolean result = cityValidationApi.isCityValid(cityName);

        
        assertTrue(result, "City should be valid.");
    }

    @Test
    void testIsCityValid_InvalidCity() {
        String cityName = "InvalidCity";  
        boolean result = cityValidationApi.isCityValid(cityName);

        assertFalse(result, "City should be invalid.");
    }

    @Test
    void testIsCityValid_NullCity() {
        String cityName = null;
        boolean result = cityValidationApi.isCityValid(cityName);

        assertFalse(result, "City should be invalid when null.");
    }

    @Test
    void testIsCityValid_EmptyCity() {
        String cityName = "";
        boolean result = cityValidationApi.isCityValid(cityName);

        // Assert that the result is false, meaning the city is not valid
        assertFalse(result, "City should be invalid when empty.");
    }

    @Test
    void testIsCityValid_APIErrorHandling() {
        
        String cityName = "SomeCityThatMayCauseError";
        
        try {
            boolean result = cityValidationApi.isCityValid(cityName);

            assertFalse(result, "City should be invalid due to API error.");
        } catch (Exception e) {
            assertTrue(e instanceof IOException || e instanceof InterruptedException, 
                    "An exception should be thrown for network issues or interruptions.");
        }
    }
}
