package in.udacity.learning.web_services;

/**
 * Created by Lokesh on 06-09-2015.
 */
public interface WebServiceParsingKeys {

    interface weatherKeys {
        String MIN = "min";
        String MAX = "max";
        String MAIN = "main";

        String PRESSURE = "pressure";
        String HUMIDITY = "humidity";
        String SPEED = "speed";
        String DEGREE = "deg";

        String WEATHER_ID = "id";
        String DESC = "description";

        String WEATHER = "weather";
        String LIST = "list";
        String TEMP = "temp";
    }

    interface locationKeys {

        String CITY = "city";
        String ID = "id";
        String LATI = "lat";
        String LONGI = "lon";
        String CITY_NAME = "name";
        String COUNTRY = "country";

        String COORD = "coord";
    }
}
