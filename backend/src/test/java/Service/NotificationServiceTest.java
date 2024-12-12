package Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import DAO.DatabaseManager;
import DAO.DBConnexion;
import weatherApp.backend.Forecast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class NotificationServiceTest {

    private NotificationService notificationService;
    private DatabaseManager mockDbManager;
    private DBConnexion mockDbConnexion;
    private ScheduledExecutorService schedulerMock;

    @BeforeEach
    void setUp() throws Exception {
        mockDbConnexion = mock(DBConnexion.class);
        mockDbManager = mock(DatabaseManager.class);
        schedulerMock = mock(ScheduledExecutorService.class);;
//        notificationService = new NotificationService(mockDbConnexion);
        notificationService = new NotificationService(mockDbConnexion, schedulerMock); 

        // Inject mock DatabaseManager into NotificationService
        NotificationService.class.getDeclaredField("dbManager").setAccessible(true);
        NotificationService.class.getDeclaredField("dbManager").set(notificationService, mockDbManager);
        
        
    }

    @Test
    void testSendDesktopNotification() throws ClassNotFoundException, SQLException {

        NotificationService spyService = Mockito.spy(notificationService);
        doNothing().when(spyService).sendDesktopNotification(anyString(), anyString());

        spyService.sendDesktopNotification("Test", "Test message");

        verify(spyService).sendDesktopNotification("Test", "Test message");
    }



    @Test
    public void testScheduleDailyNotifications() {
        int userId = 1;
        int hour = 18;
        int minute = 33;
        String city = "Paris";

        notificationService.scheduleDailyNotifications(userId, hour, minute, city);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Long> initialDelayCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> periodCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> timeUnitCaptor = ArgumentCaptor.forClass(TimeUnit.class);

        verify(schedulerMock, times(1)).scheduleAtFixedRate(runnableCaptor.capture(), initialDelayCaptor.capture(), periodCaptor.capture(), timeUnitCaptor.capture());

        assertNotNull(runnableCaptor.getValue(), "La tâche planifiée ne doit pas être nulle");
        assertEquals(24 * 60, periodCaptor.getValue(), "La période doit être de 24 heures en minutes");
        assertEquals(TimeUnit.MINUTES, timeUnitCaptor.getValue(), "L'unité de temps doit être en minutes");
    }


    @Test
    void testConvertWeatherDataToForecast() throws Exception {
        String jsonData = """
            {
                "forecast": {
                    "forecastday": [
                        {
                            "hour": [
                                {"time": "2023-12-07 09:00", "temp_c": 22.5, "chance_of_rain": 10, "wind_kph": 15},
                                {"time": "2023-12-07 10:00", "temp_c": 23.0, "chance_of_rain": 20, "wind_kph": 10}
                            ]
                        }
                    ]
                }
            }
        """;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode weatherDataJson = objectMapper.readTree(jsonData);

        Forecast forecast = notificationService.ConvertWeatherDataToForecast(weatherDataJson);

        // Check if the data was correctly parsed
        List<String> expectedHourlyForecasts = new ArrayList<>();
        expectedHourlyForecasts.add("2023-12-07 09:00 22.5 10.0 15.0");
        expectedHourlyForecasts.add("2023-12-07 10:00 23.0 20.0 10.0");

        assertEquals(expectedHourlyForecasts, forecast.getHourlyForecasts());
    }

    @Test
    void testConvertStringToForecast() {
        String forecastString = "2023-12-07 09:00 22.5 10.0 15.0";

        Forecast forecast = NotificationService.convertStringToForecast(forecastString);

        assertEquals("09:00", forecast.getDate());
        assertEquals(22.5, forecast.getTemperatureMax());
        assertEquals(10.0, forecast.getPrecipitationProbability());
        assertEquals(15.0, forecast.getWindSpeed());
    }

    @Test
    void testAlertConditionsMeteo_NoErrors() throws Exception {

        // JSON simulé de prévisions météo
        String weatherJson = """
            {
                "forecast": {
                    "forecastday": [
                        {
                            "hour": [
                                {
                                    "time": "2024-12-07 14:00",
                                    "temp_c": 45.0,
                                    "chance_of_rain": 10.0,
                                    "wind_kph": 30.0
                                },
                                {
                                    "time": "2024-12-07 15:00",
                                    "temp_c": -5.0,
                                    "chance_of_rain": 20.0,
                                    "wind_kph": 60.0
                                },
                                {
                                    "time": "2024-12-07 16:00",
                                    "temp_c": 25.0,
                                    "chance_of_rain": 80.0,
                                    "wind_kph": 40.0
                                }
                            ]
                        }
                    ]
                }
            }
        """;

        // Convertir JSON string en JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode weatherDataJson = objectMapper.readTree(weatherJson);

        NotificationService spyService = Mockito.spy(notificationService);
        doNothing().when(spyService).sendDesktopNotification(anyString(), anyString());

        assertDoesNotThrow(() -> spyService.AlertConditionsMeteo(weatherDataJson));
    }


    @Test
    void testAlertConditionsMeteo_NonVerifiees() throws Exception {

    	String jsonData = """
            {
                "forecast": {
                    "forecastday": [
                        {
                            "hour": [
                                {"time": "2023-12-07 09:00", "temp_c": 20.0, "chance_of_rain": 50, "wind_kph": 30}
                            ]
                        }
                    ]
                }
            }
        """;

        // Préparer l'objet JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode weatherDataJson = objectMapper.readTree(jsonData);

        NotificationService notificationService = spy(new NotificationService(mock(DBConnexion.class)));
        notificationService.AlertConditionsMeteo(weatherDataJson);

        // Vérifier que sendDesktopNotification n'est jamais appelé
        verify(notificationService, never()).sendDesktopNotification(anyString(), anyString());
    }
}
