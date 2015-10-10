package in.udacity.learning.model;

/**
 * Created by Lokesh on 10-10-2015.
 */
public class LocationAttribute {

    String setting;
    String cityName;
    String lati;
    String longi;

    public LocationAttribute(String setting, String cityName, String lati, String longi) {
        this.setting = setting;
        this.cityName = cityName;
        this.lati = lati;
        this.longi = longi;
    }

    public String getSetting() {
        return setting;
    }

    public String getCityName() {
        return cityName;
    }

    public String getLati() {
        return lati;
    }

    public String getLongi() {
        return longi;
    }
}
