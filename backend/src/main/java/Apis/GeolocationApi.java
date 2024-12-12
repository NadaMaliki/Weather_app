package Apis;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeolocationApi {

    private String ipAddress;
    private String apiKey = "a3e8b1ba-ec8d-4106-8b97-b06065f4a418"; // Votre clé API Apiip

    public String getIpAddress() {
        if (this.ipAddress == null) {
            fetchIpAddress();
        }
        return this.ipAddress;
    }

    private void fetchIpAddress() {
        String apiUrl = "https://api.ipify.org?format=json";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            this.ipAddress = parseIpFromJson(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            this.ipAddress = "Unable to fetch IP";
        }
    }

    private String parseIpFromJson(String jsonResponse) {
        int startIndex = jsonResponse.indexOf(":\"") + 2;
        int endIndex = jsonResponse.indexOf("\"", startIndex);
        return jsonResponse.substring(startIndex, endIndex);
    }

    public String getGeolocationInfo() {
        String ip = getIpAddress();
        if (ip.equals("Unable to fetch IP")) {
            return "Unable to fetch geolocation info due to missing IP.";
        }

        try {
            // Construction correcte de l'URL
            String apiUrl = "https://apiip.net/api/check?ip=" + URLEncoder.encode(ip, StandardCharsets.UTF_8) +
                            "&accessKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body(); // Retourne la réponse brute de l'API
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Unable to fetch geolocation info.";
        }
    }

    public static void main(String[] args) {
        GeolocationApi geoApi = new GeolocationApi();

        // Adresse IP publique
        String ipAddress = geoApi.getIpAddress();
        System.out.println("Adresse IP publique : " + ipAddress);

        // Informations de géolocalisation
        String geolocationInfo = geoApi.getGeolocationInfo();
        System.out.println("Informations de géolocalisation : " + geolocationInfo);
        }
}

