import java.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;


@Getter
@Setter
@AllArgsConstructor
public class Forecast {
    private String date;
    private double temperatureMax;
    private double temperatureMin;
    private double precipitationProbability;
    private String description;
    private double humidity;
    private double windSpeed;
    private List<String> hourlyForecasts;
}