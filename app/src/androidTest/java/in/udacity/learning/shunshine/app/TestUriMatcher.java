package in.udacity.learning.shunshine.app;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.dbhelper.WeatherProvider;

/**
 * Created by Lokesh on 27-09-2015.
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final String LOCATION_QUERY = "London, UK";
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    private static final long TEST_LOCATION_ID = 10L;

    // content://in.udacity.learning.shunshine.app/weather"
    private static final Uri TEST_WEATHER_DIR = WeatherContract.WeatherEntry.CONTENT_URI;
    private static final Uri TEST_WEATHER_WITH_LOCATION_DIR = WeatherContract.WeatherEntry.buildWeatherLocation(LOCATION_QUERY);
    private static final Uri TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(LOCATION_QUERY, TEST_DATE);

    // content://in.udacity.learning.shunshine.app/location"
    private static final Uri TEST_LOCATION_DIR = WeatherContract.LocationEntry.CONTENT_URI;

    /*
      Students: This function tests that your UriMatcher returns the correct integer value
      for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
      ready to test your UriMatcher.
   */
    public void testUriMatcher() {
        UriMatcher testMatcher = WeatherProvider.buildUriMatcher();

        assertEquals("Error: The WEATHER URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_DIR), WeatherProvider.WEATHER);
        assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_DIR), WeatherProvider.WEATHER_WITH_LOCATION);
        assertEquals("Error: The WEATHER WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR), WeatherProvider.WEATHER_WITH_LOCATION_AND_DATE);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_LOCATION_DIR), WeatherProvider.LOCATION);
    }

}
