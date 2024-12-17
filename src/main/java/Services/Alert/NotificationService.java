package Services.Alert;

import Dao.DatabaseManager;
import Dao.DBConnexion;
import Services.Api.*;
import Model.Forecast;
import Model.User;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Dao.UserPreferencesManager;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class NotificationService {
	
    DatabaseManager dbManager;
    ScheduledExecutorService scheduler;
    ScheduledExecutorService schedule_alert = Executors.newScheduledThreadPool(1);


    public NotificationService(DBConnexion dbConnexion) throws SQLException {
        this.dbManager = new DatabaseManager(dbConnexion);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    public NotificationService(DBConnexion dbConnexion, ScheduledExecutorService scheduler) { 
    	this.dbManager = new DatabaseManager(dbConnexion); 
    	this.scheduler = scheduler != null ? scheduler : Executors.newScheduledThreadPool(1); 	
    }
    
    // Envoyer une notification desktop au user
    public void sendDesktopNotification(String title, String message) {
    	
        if (!SystemTray.isSupported()) {
            System.err.println("Notifications desktop non prises en charge sur ce système.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("w/backend/weather_icon.png"); 
            image = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            TrayIcon trayIcon = new TrayIcon(image, "My Weather App");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Alerte Météo");
            tray.add(trayIcon);

            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

            Thread.sleep(7000); 
            tray.remove(trayIcon);
        } catch (Exception e) {
        	e.printStackTrace(); 
            System.err.println("Erreur lors de l'envoi de la notification desktop : " + e.getMessage());
        }
    }

    //planifier une notification pour chaque jour
    public void scheduleDailyNotifications(int userId, int hour, int minute, String city) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
            	 DBConnexion dbConnexion = new DBConnexion();
                 UserPreferencesManager userpreferences = new UserPreferencesManager(dbConnexion);
                 
                // Récupérer les informations nécessaires pour envoyer une notification
                User user = dbManager.getUserById(userId); 
                String userName = user.getPrenom(); 
                String unit = userpreferences.getUnit(userId);

                double averageTemperature = dbManager.getAverageTemperatureForDay(city);
                double rainChance = dbManager.getRainChanceForToday(city);
                double convertedTemperature = userpreferences.convertTemperature(averageTemperature, unit);
                String convertedTemperatureStr = String.format("%.2f", convertedTemperature);

                String message = String.format(
                    "Bonjour %s, la température moyenne du jour à %s est de %s %s et la probabilité de pluie est de %.2f%%.",
                    userName, city, convertedTemperatureStr,unit, rainChance
                );

                sendDesktopNotification("Daily Notification Météo", message);

            } catch (Exception e) {
                handleNotificationError(e);
            }
        }, getInitialDelayForSpecificTime(hour, minute), 24 * 60, TimeUnit.MINUTES); // Notifications toutes les 24 heures
    }

    
    private long getInitialDelayForSpecificTime(int hour, int minute) {
        long currentTimeMillis = System.currentTimeMillis();
        long targetTimeMillis = getNextTargetTimeMillis(hour, minute);

        return TimeUnit.MILLISECONDS.toMinutes(targetTimeMillis - currentTimeMillis);
    }

    // Méthode pour obtenir le temps cible en millisecondes depuis Epoch
    private long getNextTargetTimeMillis(int hour, int minute) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // Si l'heure cible est déjà passée aujourd'hui, planifier pour demain
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }

    // Gérer les erreurs lors de l'envoi des notifications
    public void handleNotificationError(Exception error) {
        System.err.println("Erreur lors de l'envoi de la notification : " + error.getMessage());
        error.printStackTrace(); 
    }
    
    // convertir les info meteorologique de json vers Forcast
    public Forecast ConvertWeatherDataToForecast(JsonNode weatherDataJson) {
            JsonNode forecastDays = weatherDataJson.get("forecast").get("forecastday");

                // Prévisions horaires
                List<String> hourlyForecasts = new ArrayList<>();
                JsonNode hourlyData = forecastDays.get(0).get("hour");
                for (JsonNode hour : hourlyData) {
                    String hourTime = hour.get("time").asText();
                    double hourTemp = hour.get("temp_c").asDouble();
                    double hourRain = hour.get("chance_of_rain").asDouble();
                    double windSpeed = hour.get("wind_kph").asDouble();

                    hourlyForecasts.add(hourTime + " " + hourTemp + " " + hourRain + " " + windSpeed );
                }

                Forecast forecast = new Forecast(hourlyForecasts);
            
        return forecast;
    }
    
    // convertir les infos heure par heure stocker de String vers Forcast
    public static Forecast convertStringToForecast(String forecastString) {
        try {
            // Séparer les données de la chaîne
            String[] parts = forecastString.split(" ");
            
            // Extraire chaque champ
            String hour = parts[1];
            double temperature = Double.parseDouble(parts[2]);
            double precipitationProbability = Double.parseDouble(parts[3]);
            double windSpeed = Double.parseDouble(parts[4]);
            
            // Créer et retourner un objet Forecast
            return new Forecast(hour, temperature, precipitationProbability, windSpeed);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de chaîne invalide : " + forecastString, e);
        }
    }
    
    public void AlertConditionsMeteo(JsonNode weatherDataJson) {
        try {
        	
            // Convertir les données météo JSON en objet Forecast
            Forecast weatherData = ConvertWeatherDataToForecast(weatherDataJson);

            LocalTime now = LocalTime.now();
            
            List<String> StringHourlyForecasts = weatherData.getHourlyForecasts(); 
            List<Forecast> HourlyForecasts = new ArrayList<>();
            
            for (String StringHourlyForecast : StringHourlyForecasts) {
            	Forecast forecast = convertStringToForecast(StringHourlyForecast);
            	HourlyForecasts.add(forecast);
            }

            LocalTime nextHour = now.plusHours(1);

         // Parcourir les informations météo heure par heure pour une seule journée
            for (Forecast hourly : HourlyForecasts ) {

                    String StringeventTime = hourly.getDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime eventTime = LocalTime.parse(StringeventTime, formatter);

                    String hour = eventTime.format(DateTimeFormatter.ofPattern("HH")); 
                    // Heure uniquement
                    if (eventTime.getHour() == nextHour.getHour()) {
                        System.out.println("Vérification pour l'heure suivante : " + nextHour);

                            if (hourly.getPrecipitationProbability() > 70) {
                                sendDesktopNotification(
                                    "Alerte Météo",
                                    String.format("Une pluie abondante est attendue à %sh !", hour)
                                );
                            }  
                            if (hourly.getTemperatureMax() > 40) {
                                sendDesktopNotification(
                                    "Alerte Météo",
                                    String.format("Alerte Canicule annoncée à %sh !", hour)
                                );
                            } else if (hourly.getTemperatureMax() < 0) {
                                sendDesktopNotification(
                                    "Alerte Météo",
                                    String.format("Alerte Froid extrême prévue à %sh !", hour)
                                );
                            } 
                            if (hourly.getWindSpeed() > 50) {
                                sendDesktopNotification(
                                    "Alerte Météo",
                                    String.format("Alerte des vents violents sont attendus à %sh. Restez à l'intérieur si possible.", hour)
                                );
                            }
                        }
                    }            
            
        } catch (Exception e) {
            handleNotificationError(e);
        }
    }



    public static void main(String[] args) {
        try {
            // Initialiser la connexion à la base de données (paramètres fictifs)
            DBConnexion dbConnexion = new DBConnexion();

            // Initialiser le service de notifications
            NotificationService notificationService = new NotificationService(dbConnexion);

            // Tester une notification immédiate (desktop)
            notificationService.sendDesktopNotification(
                "Test Notification",
                "Ceci est une notification de test sur votre bureau."
            );

            
            // Planifier des notifications quotidiennes pour un utilisateur fictif (ID utilisateur = 1)
            notificationService.scheduleDailyNotifications(1, 22, 43, "marrakech");

            System.out.println("Notification planifiée pour 8h30. Appuyez sur Ctrl+C pour arrêter.");
            Thread.currentThread().join();


    		 WeatherApi weatherApi = new WeatherApi();
			 JsonNode weatherDataJson = weatherApi.getWeatherForecastByCity("marrakech");
			 notificationService.AlertConditionsMeteo(weatherDataJson);
			 Forecast forecast = notificationService.ConvertWeatherDataToForecast(weatherDataJson);
			 for (String hourly : forecast.getHourlyForecasts()) {
	                System.out.println(hourly);
	            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
