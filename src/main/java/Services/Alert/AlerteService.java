package Services.Alert;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import Model.User;
import Services.Api.*;
import Dao.*;


public class AlerteService {

	public final EmailService emailService;
	public final NotificationService notificationService;	
	public final ScheduledExecutorService scheduler;
	public final DBConnexion dbConnexion;
	public final DatabaseManager dbManager;
	public final UserPreferencesManager userPreferences;
	public final WeatherApi weatherApi;

	// Constructeur principal
	public AlerteService(DBConnexion dbConnexion, DatabaseManager dbManager, UserPreferencesManager userPreferences,
						 WeatherApi weatherApi, EmailService emailService, NotificationService notificationService,
						 ScheduledExecutorService scheduler) {
		this.dbConnexion = dbConnexion;
		this.dbManager = dbManager;
		this.userPreferences = userPreferences;
		this.weatherApi = weatherApi;
		this.emailService = emailService;
		this.notificationService = notificationService;
		this.scheduler = scheduler != null ? scheduler : Executors.newScheduledThreadPool(1);
	}
	
	
	public void afficherDailyAlerteUtilisateur(int userId) throws ClassNotFoundException {
        User user = dbManager.getUserById(userId);
        if (user == null) {
            System.err.println("Utilisateur non trouvé avec l'ID : " + userId);
            return;
        }

        String city = userPreferences.getDefaultCity(userId);
        if (city == null || city.isEmpty()) {
            System.err.println("Aucune ville définie pour l'utilisateur.");
            return;
        }

        int hour = 8;
        int minute = 00;

        if (user.getAlerte() == 1 || user.getAlerte() == 3) {
            emailService.scheduleDailyEmailNotifications(userId, hour, minute, city);
        }

        if (user.getAlerte() == 2 || user.getAlerte() == 3) {
            notificationService.scheduleDailyNotifications(userId, hour, minute, city);
        }
    }

	public void checkAndSendWeatherAlert(int userId) throws SQLException {
		User user = dbManager.getUserById(userId);
		if (user == null) {
			System.err.println("Utilisateur non trouvé pour l'ID : " + userId);
			return;
		}

		if (user.getAlerte() == 2 || user.getAlerte() == 3) {
			System.out.println("Alerte activée pour l'utilisateur.");

			scheduler.scheduleAtFixedRate(() -> {
				try {
					String city = userPreferences.getDefaultCity(userId);
					if (city == null || city.isEmpty()) {
						System.err.println("Ville non définie pour l'utilisateur.");
						return;
					}
					JsonNode weatherDataJson = weatherApi.getWeatherForecastByCity(city);
					notificationService.AlertConditionsMeteo(weatherDataJson);
					System.out.println("Alerte météo envoyée pour la ville : " + city);
				} catch (Exception e) {
					System.err.println("Erreur lors de l'exécution des alertes météo : " + e.getMessage());
					e.printStackTrace();
				}
			}, 0, 1, TimeUnit.HOURS); // Exécuter immédiatement, puis toutes les heures
		} else {
			System.out.println("Notifications non autorisées!");
		}
	}


	// Méthode main pour tester le service
	public static void main(String[] args) {
		try {
			// Initialisation des dépendances
			DBConnexion dbConnexion = new DBConnexion();
			DatabaseManager dbManager = new DatabaseManager(dbConnexion);
			UserPreferencesManager userPreferences = new UserPreferencesManager(dbConnexion);
			WeatherApi weatherApi = new WeatherApi();
			EmailService emailService = new EmailService(dbConnexion);
			NotificationService notificationService = new NotificationService(dbConnexion);
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

			// Création de l'instance du service
			AlerteService alerteService = new AlerteService(dbConnexion, dbManager, userPreferences, weatherApi,
					emailService, notificationService, scheduler);

			// ID de l'utilisateur à tester
			int userId = 125; // Remplacez par un ID existant dans votre base de données

			// Récupérer l'utilisateur depuis la base de données
			User user = dbManager.getUserById(userId);

			if (user != null) {
				System.out.println("Utilisateur trouvé : " + user.getNom() + " " + user.getPrenom());
				System.out.println("Alerte activée : " + user.getAlerte());

				alerteService.afficherDailyAlerteUtilisateur(userId);
				alerteService.checkAndSendWeatherAlert(userId);
			} else {
				System.out.println("Aucun utilisateur trouvé avec l'ID : " + userId);
			}
		} catch (Exception e) {
			System.err.println("Erreur lors de l'exécution : " + e.getMessage());
			e.printStackTrace();
		}
	}
}
