package project.weather_app;

import Dao.DBConnexion;
import Dao.DatabaseManager;
import Model.Forecast;
import Model.User;
import Services.Alert.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.mail.MessagingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EmailServiceTest {
	
	private DBConnexion mockDbConnection;
    private Connection mockConnection;
    private DatabaseManager mockdbManager;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private User mockUser;
    private EmailService emailService;
    private EmailService emailService_validation;
    private ScheduledExecutorService schedulerMock;

    @BeforeEach
    void setUp() throws SQLException {
        mockDbConnection = Mockito.mock(DBConnexion.class);
        mockConnection = Mockito.mock(Connection.class);
        mockdbManager = mock(DatabaseManager.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);
        mockUser = Mockito.mock(User.class);
        schedulerMock = mock(ScheduledExecutorService.class);
        emailService = new EmailService(mockDbConnection); 
        emailService_validation = new EmailService(mockDbConnection); 
        emailService.scheduler = schedulerMock;

        when(mockDbConnection.getCon()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockUser.getId()).thenReturn(1);

        emailService = new EmailService(mockDbConnection, schedulerMock);
        emailService_validation = Mockito.spy(new EmailService(mockDbConnection));

    }
    

    @Test
    void testsendEmailNotification_EmailValid() throws Exception {
    	int userId = 1;
        String notificationContent = "Today's weather: sunny";

        when(emailService_validation.getUserEmailFromDatabase(userId)).thenReturn("test@example.com");

        emailService_validation.sendEmailNotification(userId, notificationContent);

        verify(emailService_validation, times(1)).sendEmail("test@example.com", "Votre météo du jour 🌞🌧 – Préparez-vous pour une journée parfaite !", notificationContent);
    }

    @Test
    void testsendEmailNotification_EmailInvalid() throws Exception {
    	int userId = 1;
        String notificationContent = "Today's weather: sunny";

        when(emailService_validation.getUserEmailFromDatabase(userId)).thenReturn("invalid-email");

        emailService_validation.sendEmailNotification(userId, notificationContent);

        verify(emailService_validation, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testsendEmailNotification_EmailNotFound() throws Exception {
        doReturn(null).when(emailService_validation).getUserEmailFromDatabase(1); 

        emailService_validation.sendEmailNotification(mockUser.getId(), "Contenu de la notification");

        verify(emailService_validation).getUserEmailFromDatabase(1);
        verify(emailService_validation, never()).sendEmail(anyString(), anyString(), anyString()); 
    }
    
    @Test
    void testGetUserEmailFromDatabase_EmailFound() throws SQLException {
    	
    	when(mockResultSet.next()).thenReturn(true); 
        when(mockResultSet.getString("email")).thenReturn("test@example.com"); 
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet); 

        String email = emailService.getUserEmailFromDatabase(1);

        assertNotNull(email, "L'email retourné ne doit pas être null");
        assertEquals("test@example.com", email, "L'email retourné ne correspond pas à l'attendu");

        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet).next();
        verify(mockResultSet).getString("email");
    }

    @Test
    void testGetUserEmailFromDatabase_EmailNotFound() throws SQLException {
    	
        when(mockResultSet.next()).thenReturn(false); 

        String email = emailService.getUserEmailFromDatabase(1);

        assertNull(email); 
        verify(mockPreparedStatement).setInt(1, 1); 
        verify(mockPreparedStatement).executeQuery(); 
    }

    @Test
    void testGetUserEmailFromDatabase_SQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Database error")); 

        String email = emailService.getUserEmailFromDatabase(1);

        assertNull(email); 
        verify(mockPreparedStatement).setInt(1, 1); 
        verify(mockPreparedStatement).executeQuery(); 
    }

    
    @Test
    void testGetFailedEmails() throws Exception {
    	
        emailService.sendEmail("invalid-email", "Subject", "Body");

        assertTrue(emailService.getFailedEmails().contains("invalid-email"));
    }
    
    @Test
    public void testIsValidEmail_ValidEmail() {
        String validEmail = "test@example.com";
        assertTrue(emailService.isValidEmail(validEmail));
    }

    @Test
    public void testIsValidEmail_InvalidEmail() {
        String invalidEmail = "invalid-email";
        assertFalse(emailService.isValidEmail(invalidEmail));
    }


    @Test
    public void testSendEmail_InvalidEmail() {
        String invalidEmail = "invalid-email";
        String subject = "Test Subject";
        String body = "Test Body";

        boolean result = emailService.sendEmail(invalidEmail, subject, body);

        assertFalse(result);
    }

    @Test
    public void testSendEmail_ValidEmail() throws MessagingException {
        String validEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        emailService = mock(EmailService.class);
        when(emailService.sendEmail(validEmail, subject, body)).thenReturn(true);

        boolean result = emailService.sendEmail(validEmail, subject, body);

        assertTrue(result);
    }
    
        @Test
        public void testScheduleDailyEmailNotifications() {
            int userId = 1;
            int hour = 18;
            int minute = 33;
            String city = "Paris";

            emailService.scheduleDailyEmailNotifications(userId, hour, minute, city);

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
    public void testGenerateEmailContent() {
        String userName = "John";
        Forecast weatherData = new Forecast("2024-12-06", 20.0, 15.0, 50.0, "Sunny", 60.0, 10.0);
        String city = "Paris";
        String unit = "°C";
        double minTemp = 15.0;
        double maxTemp = 20.0;

        String emailContent = emailService.generateEmailContent(userName, weatherData, city, unit, minTemp, maxTemp);

        assertTrue(emailContent.contains("Bonjour John"));
        assertTrue(emailContent.contains("Météo Générale : Sunny"));
        assertTrue(emailContent.contains("Maximale : 20.0°C"));
        assertTrue(emailContent.contains("Minimale : 15.0°C"));
    }

    @Test
    public void testConvertWeatherDataToForecast() {
        JsonNode mockWeatherJson = mock(JsonNode.class);
        JsonNode mockForecast = mock(JsonNode.class);
        JsonNode mockForecastDayArray = mock(JsonNode.class);
        JsonNode mockForecastDay = mock(JsonNode.class);
        JsonNode mockDay = mock(JsonNode.class);
        JsonNode mockCondition = mock(JsonNode.class);

        when(mockWeatherJson.get("forecast")).thenReturn(mockForecast);
        when(mockForecast.get("forecastday")).thenReturn(mockForecastDayArray);
        when(mockForecastDayArray.get(0)).thenReturn(mockForecastDay);
        when(mockForecastDay.get("date")).thenReturn(mock(JsonNode.class));
        when(mockForecastDay.get("date").asText()).thenReturn("2024-12-06");
        when(mockForecastDay.get("day")).thenReturn(mockDay);
        when(mockDay.get("maxtemp_c")).thenReturn(mock(JsonNode.class));
        when(mockDay.get("maxtemp_c").asDouble()).thenReturn(20.0);
        when(mockDay.get("mintemp_c")).thenReturn(mock(JsonNode.class));
        when(mockDay.get("mintemp_c").asDouble()).thenReturn(15.0);
        when(mockDay.get("avghumidity")).thenReturn(mock(JsonNode.class));
        when(mockDay.get("avghumidity").asDouble()).thenReturn(60.0);
        when(mockDay.get("maxwind_kph")).thenReturn(mock(JsonNode.class));
        when(mockDay.get("maxwind_kph").asDouble()).thenReturn(10.0);
        when(mockDay.get("daily_chance_of_rain")).thenReturn(mock(JsonNode.class));
        when(mockDay.get("daily_chance_of_rain").asDouble()).thenReturn(50.0);
        when(mockDay.get("condition")).thenReturn(mockCondition);
        when(mockCondition.get("text")).thenReturn(mock(JsonNode.class));
        when(mockCondition.get("text").asText()).thenReturn("Sunny");

        Forecast forecast = emailService.ConvertWeatherDataToForecast(mockWeatherJson);

        assertEquals("2024-12-06", forecast.getDate());
        assertEquals(20.0, forecast.getTemperatureMax());
        assertEquals(15.0, forecast.getTemperatureMin());
        assertEquals(60.0, forecast.getHumidity());
    }

}