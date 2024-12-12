package weatherApp.backend;

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
    
    public Forecast(String date, double tmax, double tmin, double prec, String dsp, double hum, double wind) {
    	this.date = date;
    	this.temperatureMax = tmax;
    	this.temperatureMin = tmin;
    	this.precipitationProbability = prec;
    	this.description = dsp;
    	this.humidity = hum;
    	this.windSpeed = wind;
    }

	public Forecast(List<String> hourlyForecasts) {
		this.hourlyForecasts = new ArrayList<>(hourlyForecasts);
	}  
	
	public Forecast(String date, double temp, double prec, double wind) {
    	this.date = date;
    	this.temperatureMax = temp;
    	this.precipitationProbability = prec;
    	this.windSpeed = wind;
    }

}