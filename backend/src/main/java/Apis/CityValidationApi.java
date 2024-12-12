package Apis;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;

public class CityValidationApi {

    private String apiKey = "XWrkZyPbMECnb104PyrLwg==aUIsObPh4mseMxUf"; 

    public boolean isCityValid(String cityName) {
        String apiUrl = "https://api.api-ninjas.com/v1/city?name=" + cityName;  // l'url de l'api
        
        HttpClient client = HttpClient.newHttpClient(); 
        HttpRequest request = HttpRequest.newBuilder() 
                .uri(URI.create(apiUrl))
                .header("X-Api-Key", apiKey) 
                .GET()
                .build();   // ouvrir la requete en ajoutant la clé API 
        
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); // variable contenant le résultat.
            String responseBody = response.body(); // le body de la requete

            
            System.out.println("Raw response: " + responseBody);  // juste pour voir le résultat , on peut la supprimer.

            // test de validité de la réponse (format JSON ou non)
            try {
                JSONArray jsonResponse = new JSONArray(responseBody);

                // on teste si on a du contenu , si oui , la ville est valide , sinon , la ville n'existe pas .
                if (jsonResponse.length() > 0) {
                    return true;
                }
            } catch (Exception e) {
                System.out.println("Failed to parse JSON: " + e.getMessage());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) {
        CityValidationApi cityApi = new CityValidationApi();
        
        String city = "Marrakech";
        boolean isValid = cityApi.isCityValid(city);

        System.out.println("Is the city '" + city + "' valid? " + isValid);
        }
}
