package in.udacity.learning.model;

/**
 * Created by Lokesh on 10-10-2015.
 */
public class WeatherAttribute {

    String locationId = "locationId";
    String weatherDate = "weatherDate";
    String humidity = "humidity";
    String pressure = "pressure";
    String windSpeed = "windSpeed";
    String degree = "degree";
    String max = "max";
    String min = "min";
    String description = "description";
    String weather_id = "weather_id";

    public WeatherAttribute(String locationId, String weatherDate, String humidity, String pressure, String windSpeed, String degree, String max, String min, String description, String weather_id) {
        this.locationId = locationId;
        this.weatherDate = weatherDate;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.degree = degree;
        this.max = max;
        this.min = min;
        this.description = description;
        this.weather_id = weather_id;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getWeatherDate() {
        return weatherDate;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getDegree() {
        return degree;
    }

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }

    public String getDescription() {
        return description;
    }

    public String getWeather_id() {
        return weather_id;
    }
}
