package project.weather_app;

import static org.mockito.Mockito.*; 
import static org.junit.jupiter.api.Assertions.*; 

import java.sql.SQLException; 
import java.util.concurrent.ScheduledExecutorService; 
import java.util.concurrent.TimeUnit; 
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test; 
import org.mockito.ArgumentCaptor;

import Services.Api.*; 
import Dao.DBConnexion; 
import Dao.DatabaseManager; 
import Dao.UserPreferencesManager;
import Services.Alert.AlerteService;
import Services.Alert.EmailService;
import Services.Alert.NotificationService;
import Model.User;

class AlerteServiceTest {

	private AlerteService alerteService; 
	private DBConnexion dbConnexionMock; 
	private DatabaseManager dbManagerMock; 
	private UserPreferencesManager userPreferencesMock; 
	private WeatherApi weatherApiMock; 
	private EmailService emailServiceMock; 
	private NotificationService notificationServiceMock; 
	private ScheduledExecutorService schedulerMock; 
	
	@BeforeEach 
	public void setUp() { 
		dbConnexionMock = mock(DBConnexion.class); 
		dbManagerMock = mock(DatabaseManager.class); 
		userPreferencesMock = mock(UserPreferencesManager.class); 
		weatherApiMock = mock(WeatherApi.class); 
		emailServiceMock = mock(EmailService.class); 
		notificationServiceMock = mock(NotificationService.class); 
		schedulerMock = mock(ScheduledExecutorService.class); 
		alerteService = new AlerteService(); 
		alerteService.dbConnexion = dbConnexionMock; 
		alerteService.dbManager = dbManagerMock; 
		alerteService.userpreferences = userPreferencesMock; 
		alerteService.weatherApi = weatherApiMock; 
		alerteService.emailService = emailServiceMock; 
		alerteService.notificationService = notificationServiceMock;
		
		alerteService = new AlerteService(dbConnexionMock, dbManagerMock, userPreferencesMock, weatherApiMock, emailServiceMock, notificationServiceMock, schedulerMock);
		
	}


	@Test
    void testAfficherDailyAlerteUtilisateur_Alerte1() throws ClassNotFoundException, SQLException {
        // Création de l'objet à tester
        AlerteService AlerteService = new AlerteService();
        AlerteService.dbManager = dbManagerMock;
        AlerteService.userpreferences = userPreferencesMock;
        AlerteService.emailservice = emailServiceMock;
        AlerteService.notificationservice = notificationServiceMock;

        // Configuration des mocks
        User mockUser = new User();
        mockUser.setAlerte(1); // Pour tester les deux cas
        when(dbManagerMock.getUserById(1)).thenReturn(mockUser);
        when(userPreferencesMock.getDefaultCity(1)).thenReturn("Paris");

        // Appel de la méthode à tester
        AlerteService.afficherDailyAlerteUtilisateur(1);

        // Vérifications
        verify(emailServiceMock, times(1)).scheduleDailyEmailNotifications(1, 18, 33, "Paris");
        verify(notificationServiceMock, never()).scheduleDailyNotifications(anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    public void testAfficherDailyAlerteUtilisateur_Alerte2() throws ClassNotFoundException, SQLException {
    	// Création de l'objet à tester
        AlerteService AlerteService = new AlerteService();
        AlerteService.dbManager = dbManagerMock;
        AlerteService.userpreferences = userPreferencesMock;
        AlerteService.emailservice = emailServiceMock;
        AlerteService.notificationservice = notificationServiceMock;

        // Configuration des mocks
        User mockUser = new User();
        mockUser.setAlerte(2); // Pour tester les deux cas
        when(dbManagerMock.getUserById(1)).thenReturn(mockUser);
        when(userPreferencesMock.getDefaultCity(1)).thenReturn("Paris");

        // Appel de la méthode à tester
        AlerteService.afficherDailyAlerteUtilisateur(1);

        verify(emailServiceMock, never()).scheduleDailyEmailNotifications(anyInt(), anyInt(), anyInt(), anyString());
        verify(notificationServiceMock, times(1)).scheduleDailyNotifications(1, 18, 33, "Paris");
    }

    @Test
    public void testAfficherDailyAlerteUtilisateur_Alerte3() throws ClassNotFoundException, SQLException {
    	// Création de l'objet à tester
        AlerteService AlerteService = new AlerteService();
        AlerteService.dbManager = dbManagerMock;
        AlerteService.userpreferences = userPreferencesMock;
        AlerteService.emailservice = emailServiceMock;
        AlerteService.notificationservice = notificationServiceMock;
        
        int userId = 1;
        User mockUser = new User();
        mockUser.setAlerte(3);
        when(dbManagerMock.getUserById(1)).thenReturn(mockUser);
        when(userPreferencesMock.getDefaultCity(1)).thenReturn("Paris");

        AlerteService.afficherDailyAlerteUtilisateur(1);

        verify(emailServiceMock, times(1)).scheduleDailyEmailNotifications(eq(userId), anyInt(), anyInt(), eq("Paris"));
        verify(notificationServiceMock, times(1)).scheduleDailyNotifications(eq(userId), anyInt(), anyInt(), eq("Paris"));
    }

    @Test
    public void testAfficherDailyAlerteUtilisateur_NoAlerte() throws ClassNotFoundException, SQLException {
    	// Création de l'objet à tester
        AlerteService AlerteService = new AlerteService();
        AlerteService.dbManager = dbManagerMock;
        AlerteService.userpreferences = userPreferencesMock;
        AlerteService.emailservice = emailServiceMock;
        AlerteService.notificationservice = notificationServiceMock;
        
        int userId = 1;
        User user = new User();
        user.setAlerte(0);
        when(dbManagerMock.getUserById(userId)).thenReturn(user);
        when(userPreferencesMock.getDefaultCity(userId)).thenReturn("Paris");

        alerteService.afficherDailyAlerteUtilisateur(userId);

        verify(emailServiceMock, never()).scheduleDailyEmailNotifications(anyInt(), anyInt(), anyInt(), anyString());
        verify(notificationServiceMock, never()).scheduleDailyNotifications(anyInt(), anyInt(), anyInt(), anyString());
    }


    
    @Test
    public void testCheckAndSendWeatherAlert_AlertEnabled() throws SQLException {
        int userId = 1;
        User user = new User();
        user.setAlerte(3);
        when(dbManagerMock.getUserById(userId)).thenReturn(user);
        when(userPreferencesMock.getDefaultCity(userId)).thenReturn("Paris");

        alerteService.checkAndSendWeatherAlert(userId);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(schedulerMock, times(1)).scheduleAtFixedRate(runnableCaptor.capture(), eq(0L), eq(1L), eq(TimeUnit.HOURS));

        assertNotNull(runnableCaptor.getValue(), "La tâche planifiée ne doit pas être nulle");
    }


    @Test
    void testCheckAndSendWeatherAlert_AlertDisabled() throws Exception {
        // Arrange
        int userId = 1;
        User mockUser = new User();
        mockUser.setAlerte(0);

        when(dbManagerMock.getUserById(userId)).thenReturn(mockUser);

        // Act
        alerteService.checkAndSendWeatherAlert(userId);

        // Assert
        verify(dbManagerMock).getUserById(userId);
        verifyNoInteractions(schedulerMock); 
    }
    
}

