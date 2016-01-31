package in.udacity.learning.web_services;

/**
 * Created by USER on 03-Sep-15.
 */
public interface WebServiceURL {

    //public static String baseURLWeatherForcast = "http://google.com/ping/?";
    public static String baseURLWeatherForcast = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    public static String QUERY = "q";
    public static String UNIT = "units";
    public static String DAYS = "cnt";
    public static String MODE = "mode";
    public static String KEYS = "appid";
    final String LAT_PARAM = "lat";
    final String LON_PARAM = "lon";

    //public static String MODE = "mode";

}
