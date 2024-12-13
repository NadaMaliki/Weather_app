package Services.Alert;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import Services.Api.*;
import Dao.DBConnexion;
import Dao.DatabaseManager;
import Dao.UserPreferencesManager;
import Model.User;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlerteService {
	
		public EmailService emailService;
		public NotificationService notificationService;
    	ScheduledExecutorService scheduler;
		DBConnexion dbConnexion;
		DatabaseManager dbManager;
		UserPreferencesManager userpreferences;
		WeatherApi weatherApi ;
		EmailService emailservice;
		NotificationService notificationservice;					

	public AlerteService (){


	}
		
		public AlerteService(DBConnexion dbConnexion, DatabaseManager dbManager, UserPreferencesManager userpreferences, WeatherApi weatherApi, EmailService emailservice, NotificationService notificationService, ScheduledExecutorService scheduler) { 
			this.dbConnexion = dbConnexion; 
			this.dbManager = dbManager; 
			this.userpreferences = userpreferences; 
			this.weatherApi = weatherApi;
			this.emailService = emailservice;
			this.notificationService = notificationService; 
			this.scheduler = scheduler != null ? scheduler : Executors.newScheduledThreadPool(1); 
			}
		
	    
		// Envoyer les notifications quotidiennes selon le choix de l'user
	   public void afficherDailyAlerteUtilisateur(int userid) throws ClassNotFoundException {
		    try {
		    	
				User user = dbManager.getUserById(userid);
		        String city = userpreferences.getDefaultCity(userid);
		        int hour = 8;
				int minute = 30;
			    
		        if(user.getAlerte() == 1 || user.getAlerte() == 3) {
					emailservice.scheduleDailyEmailNotifications(userid, hour, minute, city);
				 
				}
				
				if(user.getAlerte() == 2 || user.getAlerte() == 3) {
					notificationservice.scheduleDailyNotifications(userid, hour, minute, city);
										
				}
		    } catch (SQLException e) {
		        System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
		    }
		}
	   
		// Envoyer une notification au user en cas de condition extremes une heure à l'avance
	   public void checkAndSendWeatherAlert(int userid) throws SQLException {
		   
			User user = dbManager.getUserById(userid);
			if(user.getAlerte() == 2 || user.getAlerte() == 3) {
			    System.out.println("Alerte activée pour l'utilisateur.");

				scheduler.scheduleAtFixedRate(() -> {
		            try {
		            	
		    			NotificationService notificationservice = new NotificationService(dbConnexion);
				        String city = userpreferences.getDefaultCity(userid);
		            	JsonNode weatherDataJson = weatherApi.getWeatherForecastByCity(city);
		                System.out.println("Alerte météo envoyée pour la ville : " + city);
		            	notificationservice.AlertConditionsMeteo(weatherDataJson);
		            	
		            } catch (Exception e) {
		            	
		                System.err.println("Erreur lors de l'exécution des alertes météo : " + e.getMessage());
		                e.printStackTrace();
		            }
		        }, 0, 1, TimeUnit.HOURS); // Exécuter immédiatement, puis toutes les heures
			}
			else {
				System.out.println("Notifications non autorisées! ");
			}
	    }
	   
	   
	   public static void main(String[] args) {
	    	try {
	            // Initialiser la connexion à la base de données
	            DBConnexion dbConnexion = new DBConnexion();
	            DatabaseManager dbManager = new DatabaseManager(dbConnexion);
	            AlerteService alerteservice = new AlerteService();

	            // ID de l'utilisateur à tester
	            int userId = 1; // Remplacez par un ID existant dans votre base de données

	            // Récupérer l'utilisateur depuis la base de données
	            User user = dbManager.getUserById(userId);

	            if (user != null) {
	                System.out.println("Utilisateur trouvé : " + user.getNom() + " " + user.getPrenom());
	                System.out.println("Alerte activée : " + user.getAlerte());

	                // Tester la méthode afficherAlerteUtilisateur
	                alerteservice.afficherDailyAlerteUtilisateur(userId);
	                Thread.currentThread().join();
	                
	               // Ville pour laquelle récupérer la météo
	                alerteservice.checkAndSendWeatherAlert(userId);
	                
	            } else {
	                System.out.println("Aucun utilisateur trouvé avec l'ID : " + userId);
	            }
	        } catch (Exception e) {
	            System.err.println("Erreur lors de l'exécution : " + e.getMessage());
	            e.printStackTrace();
	        }
	   }
}
