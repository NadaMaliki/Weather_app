package project.weather_app;

import Services.Api.GeolocationApi;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

class GeolocationApiTest {

    private final GeolocationApi geolocationApi = new GeolocationApi();

    @Test
    void testGetGeolocationInfo() {
        try {
            String geolocationInfo = geolocationApi.getGeolocationInfo();
            assertNotNull(geolocationInfo, "Geolocation info should not be null.");
            assertFalse(geolocationInfo.isEmpty(), "Geolocation info should not be empty.");
        } catch (Exception e) {
            fail("Exception occurred while fetching geolocation info: " + e.getMessage());
        }
    }

    @Test
    void testParseCityFromGeolocationInfo() {
        String sampleGeolocationInfo = "{\"city\":\"Marrakech\",\"country\":\"Morocco\"}";
        try {
            String city = geolocationApi.parseCityFromGeolocationInfo(sampleGeolocationInfo);
            assertEquals("Marrakech", city, "The city should be Marrakech.");
        } catch (Exception e) {
            fail("Exception occurred while parsing city from geolocation info: " + e.getMessage());
        }
    }

    @Test
    void testParseCityFromInvalidGeolocationInfo() {
        String invalidGeolocationInfo = "{\"wrongKey\":\"noCity\"}";
        try {
            String city = geolocationApi.parseCityFromGeolocationInfo(invalidGeolocationInfo);
            assertEquals("", city, "The city should be an empty string if the geolocation info is invalid.");
        } catch (Exception e) {
            fail("Exception occurred while parsing invalid geolocation info: " + e.getMessage());
        }
    }


    @Test
    void testGetCurrentCityWithError() {
        // Simulating a network error by overriding getGeolocationInfo
        GeolocationApi faultyApi = new GeolocationApi() {
            @Override
            public String getGeolocationInfo() throws IOException, InterruptedException {
                throw new IOException("Simulated network error");
            }
        };

        // Calling getCurrentCity() should now return the default city "Paris"
        String city = faultyApi.getCurrentCity();

        // Check if the fallback value "Paris" is returned when an exception occurs
        assertEquals("Paris", city, "City should be Paris in case of an error.");
    }

    
    @Test
    void testGetCurrentCity() {
        try {
            String city = geolocationApi.getCurrentCity();
            assertNotNull(city, "Current city should not be null.");
            assertFalse(city.isEmpty(), "Current city should not be empty.");
        } catch (Exception e) {
            fail("Exception occurred while getting the current city: " + e.getMessage());
        }
    }

  
}
