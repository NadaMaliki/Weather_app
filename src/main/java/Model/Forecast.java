package Model;

import java.util.*;

import lombok.AllArgsConstructor;


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
    	this.setDate(date);
    	this.setTemperatureMax(tmax);
    	this.setTemperatureMin(tmin);
    	this.setPrecipitationProbability(prec);
    	this.setDescription(dsp);
    	this.setHumidity(hum);
    	this.setWindSpeed(wind);
    }

	public Forecast(List<String> hourlyForecasts) {
		this.setHourlyForecasts(new ArrayList<>(hourlyForecasts));
	}  
	
	public Forecast(String date, double temp, double prec, double wind) {
    	this.setDate(date);
    	this.setTemperatureMax(temp);
    	this.setPrecipitationProbability(prec);
    	this.setWindSpeed(wind);
    }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getTemperatureMax() {
		return temperatureMax;
	}

	public void setTemperatureMax(double temperatureMax) {
		this.temperatureMax = temperatureMax;
	}

	public double getTemperatureMin() {
		return temperatureMin;
	}

	public void setTemperatureMin(double temperatureMin) {
		this.temperatureMin = temperatureMin;
	}

	public double getPrecipitationProbability() {
		return precipitationProbability;
	}

	public void setPrecipitationProbability(double precipitationProbability) {
		this.precipitationProbability = precipitationProbability;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}

	public List<String> getHourlyForecasts() {
		return hourlyForecasts;
	}

	public void setHourlyForecasts(List<String> hourlyForecasts) {
		this.hourlyForecasts = hourlyForecasts;
	}

}