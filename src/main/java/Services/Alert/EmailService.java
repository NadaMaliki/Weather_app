package Services.Alert;

import java.util.*;
import Dao.DBConnexion;
import Dao.UserPreferencesManager;
import Dao.DatabaseManager;
import Services.Api.*;
import Model.Forecast;
import Model.User;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EmailService {
    private final DatabaseManager dbManager;
    private final Set<String> failedEmails = new HashSet<>();
    public ScheduledExecutorService scheduler;

    
    public EmailService(DBConnexion dbConnexion) {
        this.dbManager = new DatabaseManager(dbConnexion);
    	this.scheduler = Executors.newScheduledThreadPool(1); 
    }
    
    public EmailService(DBConnexion dbConnexion, ScheduledExecutorService scheduler) { 
        this.dbManager = new DatabaseManager(dbConnexion);
    	this.scheduler = scheduler != null ? scheduler : Executors.newScheduledThreadPool(1); 	
    }

    // Vérification de la validité de l'email
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    // Récupérer l'email de l'utilisateur depuis la base de données
    public String getUserEmailFromDatabase(int userId) {
        String email = null;
        String query = "SELECT Email FROM utilisateur WHERE Id = ?";  
        try (Connection conn = dbManager.getCon();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);  
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { 
                email = rs.getString("email");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return email;
    }


    // Méthode pour envoyer un email
    public boolean sendEmail(String userEmail, String subject, String body) {
        if (!isValidEmail(userEmail)) {
            System.err.println("Email invalide: " + userEmail);
            failedEmails.add(userEmail); // Ajouter l'email invalide à la liste des échecs
            return false;
        }

        // Configuration de l'envoi via SMTP 
        String from = "myweatherapp0@gmail.com";
        String password = "huit vtex zznp xddl"; 

        Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");  
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");  

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            failedEmails.add(userEmail); 
            return false;
        }
    }
    
    // Récupérer les emails échoués
    public Set<String> getFailedEmails() {
        return failedEmails;
    }


    // Envoyer une notification par email au user
    public void sendEmailNotification(int userId, String notificationContent) {
        try {
            String email = getUserEmailFromDatabase(userId);  
            if (email != null && isValidEmail(email)) {
                sendEmail(email, "Votre météo du jour 🌞🌧 – Préparez-vous pour une journée parfaite !", notificationContent);
            } else {
                System.err.println("Email invalide ou introuvable pour l'utilisateur avec ID : " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void scheduleDailyEmailNotifications(int userId, int hour, int minute, String city) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
            	
            	DBConnexion dbConnexion = new DBConnexion();
        		DatabaseManager dbManager = new DatabaseManager(dbConnexion);
        		UserPreferencesManager userpreferences = new UserPreferencesManager(dbConnexion);
        		WeatherApi weatherApi = new WeatherApi();
            	User user = dbManager.getUserById(userId);
			    JsonNode weatherDataJson = weatherApi.getWeatherForecastByCity(city);
			    Forecast weatherData = ConvertWeatherDataToForecast(weatherDataJson);  
	            String Unit = userpreferences.getUnit(userId);
	            double convertemin = userpreferences.convertTemperature(weatherData.getTemperatureMin(),Unit);
	            double convertemax = userpreferences.convertTemperature(weatherData.getTemperatureMax(),Unit);
	            
	            String userName = user.getPrenom();
			    StringBuilder emailContent = new StringBuilder();
			    emailContent.append(generateEmailContent(userName, weatherData, city, Unit, convertemin, convertemax));

			    sendEmailNotification(userId, emailContent.toString());
			    
            } catch (Exception e) {
                handleNotificationError(e);
            }
        }, getInitialDelayForSpecificTime(hour, minute), 24 * 60, TimeUnit.MINUTES); 
    }
    
    public void handleNotificationError(Exception error) {
        System.err.println("Erreur lors de l'envoi de l'email : " + error.getMessage());
        error.printStackTrace();  
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

    
    
    // Générer le contenu de l'email 
    public String generateEmailContent(String userName, Forecast weatherData, String city, String unit, double convertemin, double convertemax) {
        StringBuilder emailContent = new StringBuilder();

        // Introduction
        emailContent.append("Bonjour ").append(userName).append(",\n");
        emailContent.append("Prêt·e à affronter une nouvelle journée ? Voici ce que le ciel vous réserve aujourd'hui à ").append(city).append(":\n\n ");
        
        // Astuces pour la journée
        emailContent.append("💡 Astuces pour votre journée :\n");
        emailContent.append(getTips(weatherData)).append("\n\n");

        // Informations générales sur la météo
        emailContent.append("📅 Date : ").append(weatherData.getDate()).append("\n");
        emailContent.append("🌤 Météo Générale : ").append(weatherData.getDescription()).append("\n\n");
       
        // Températures du jour
        weatherData.setTemperatureMax(convertemax);
        weatherData.setTemperatureMin(convertemin);
        emailContent.append("🌡 Températures du jour :\n");
        emailContent.append("Maximale : ").append(weatherData.getTemperatureMax()).append(unit).append("\n");
        emailContent.append("Minimale : ").append(weatherData.getTemperatureMin()).append(unit).append("\n\n");

        // Humidité et précipitations
        emailContent.append("💧 Humidité et précipitations :\n");
        emailContent.append("Humidité moyenne : ").append(weatherData.getHumidity()).append("%\n");
        emailContent.append("Probabilité de pluie : ").append(weatherData.getPrecipitationProbability()).append("%\n\n");

        // Vent
        emailContent.append("💨 Vent :\n");
        emailContent.append("Vitesse : ").append(weatherData.getWindSpeed()).append(" km/h\n");
        emailContent.append("Direction : ").append(getWindDirection(weatherData.getWindSpeed())).append("\n\n");

        // Conclusion
        emailContent.append("On reste à votre disposition pour que chaque jour soit un peu plus ensoleillé ! 🌟\n\n");
        emailContent.append("Belle journée,\n");
        emailContent.append("L’équipe My Weather App");

        return emailContent.toString();
    }
    
    // convertir les données météo de JSON en objet Forecast
    public Forecast ConvertWeatherDataToForecast(JsonNode weatherDataJson) {
        String date = weatherDataJson.get("forecast").get("forecastday").get(0).get("date").asText();
        double tempMax = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("maxtemp_c").asDouble();
        double tempMin = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("mintemp_c").asDouble();
        double humidity = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("avghumidity").asDouble();
        double windSpeed = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("maxwind_kph").asDouble();
        double precipitationProbability = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("daily_chance_of_rain").asDouble();
        String description = weatherDataJson.get("forecast").get("forecastday").get(0).get("day").get("condition").get("text").asText();

        return new Forecast(date, tempMax, tempMin, precipitationProbability, description, humidity, windSpeed);
    }



    // Vérifier la direction du vent
    public String getWindDirection(double windDirection) {
        if (windDirection >= 0 && windDirection < 45) return "Nord";
        if (windDirection >= 45 && windDirection < 90) return "Nord-Est";
        if (windDirection >= 90 && windDirection < 135) return "Est";
        if (windDirection >= 135 && windDirection < 180) return "Sud-Est";
        if (windDirection >= 180 && windDirection < 225) return "Sud";
        if (windDirection >= 225 && windDirection < 270) return "Sud-Ouest";
        if (windDirection >= 270 && windDirection < 315) return "Ouest";
        return "Nord-Ouest";
    }

    
    // Tips (dans l'email) selon les conditions météorologiques
    public String getTips(Forecast weatherData) {
        if (weatherData.getPrecipitationProbability() > 70) {
            return "Une pluie abondante est attendue. Préparez un imperméable et des bottes !";
        } else if (weatherData.getPrecipitationProbability() > 50) {
            return "Préparez un parapluie si la pluie est prévue.";
        } else if (weatherData.getTemperatureMax() > 35) {
            return "Canicule annoncée ! Hydratez-vous bien et évitez l'exposition directe au soleil.";
        } else if (weatherData.getTemperatureMax() > 25) {
            return "Sortez vos lunettes de soleil pour une journée ensoleillée.";
        } else if (weatherData.getTemperatureMin() < 0) {
            return "Attention au gel ! Portez des vêtements chauds et protégez-vous du froid.";
        } else if (weatherData.getTemperatureMin() < 10) {
            return "Habillez-vous chaudement si les températures baissent ce soir !";
        } else if (weatherData.getWindSpeed() > 50) {
            return "Des vents violents sont prévus. Restez à l'intérieur si possible.";
        } else if (weatherData.getHumidity() > 80) {
            return "L'humidité est élevée aujourd'hui ! Cela pourrait être inconfortable.";
        }
        return "Bonne journée sans conditions météorologiques extrêmes !";
    }


    public static void main(String[] args) {
        DBConnexion dbConnexion = new DBConnexion();
        EmailService emailService = new EmailService(dbConnexion); // Create an instance of EmailService

		User user = new User(1);
		

		String notificationContent = "test";
		emailService.sendEmailNotification(user.getId(), notificationContent);

		boolean isSent = emailService.sendEmail("malikinada2004@gmail.com", "Test", "This is a test email.");
		System.out.println("Email sent: " + isSent);
         
		emailService.scheduleDailyEmailNotifications(1, 14, 51, "marrakech");

    }

}
