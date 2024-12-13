package Services.Api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeolocationApi {
    private final String API_URL = "http://ip-api.com/json";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public GeolocationApi() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String getGeolocationInfo() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String parseCityFromGeolocationInfo(String geolocationInfo) throws IOException {
        JsonNode root = mapper.readTree(geolocationInfo);
        return root.path("city").asText();
    }

    public String getCurrentCity() {
        try {
            String geolocationInfo = getGeolocationInfo();
            return parseCityFromGeolocationInfo(geolocationInfo);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la ville: " + e.getMessage());
            return "Paris"; // Ville par défaut en cas d'erreur
        }
    }
}
