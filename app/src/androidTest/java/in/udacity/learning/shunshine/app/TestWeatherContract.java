package in.udacity.learning.shunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.test.AndroidTestCase;

import in.udacity.learning.dbhelper.WeatherContract;

/**
 * Created by Lokesh on 27-09-2015.
 */
public class TestWeatherContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_WEATHER_LOCATION = "/North Pole";
    private static final String TEST_ID = "1";
    private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014

    /*
        Uncomment this out to test your weather location function.
     */
    public void testBuildWeatherLocation() {
        Uri weatherLocation = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_WEATHER_LOCATION);
        assertNotNull("Error: Null Uri returned.Must fill-in buildWeatherLocation" + "WeatherContract.",weatherLocation);
        assertEquals("Error: Weather location not properly appended to the end of the Uri",
                TEST_WEATHER_LOCATION, weatherLocation.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",weatherLocation.toString(),
                "content://in.udacity.learning.shunshine.app/weather/%2FNorth%20Pole");
    }

    public void testBuildWeatherUri()
    {
        Uri uri = WeatherContract.WeatherEntry.buildWeatherUri(Integer.parseInt(TEST_ID));
        assertNotNull("Error: Null Uri returned.Must fill-in buildWeatherLocation" + "WeatherContract.",uri);

        assertEquals("Error: Weather not properly appended to the end of the Uri",
                TEST_ID, uri.getLastPathSegment());
        assertEquals("Error: Weather Uri doesn't match our expected result", uri.toString(),
                "content://in.udacity.learning.shunshine.app/weather/1");
    }

    public void testBuildWeatherLocationUri()
    {
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocation(TEST_ID);
        assertNotNull("Error: Null Uri returned.Must fill-in buildWeatherLocation" + "WeatherContract.",uri);

        assertEquals("Error: Weather not properly appended to the end of the Uri",
                TEST_ID, uri.getLastPathSegment());
        assertEquals("Error: Weather Uri doesn't match our expected result", uri.toString(),
                "content://in.udacity.learning.shunshine.app/weather/1");
    }

    public void testBuildLocationUri()
    {
        Uri locationUri = WeatherContract.LocationEntry.buildLocationUri(Integer.parseInt(TEST_ID));
        assertNotNull("Error: Null Uri returned.Must fill-in buildWeatherLocation" + "WeatherContract.",locationUri);

        assertEquals("Error: location not properly appended to the end of the Uri",
                TEST_ID, locationUri.getLastPathSegment());
        assertEquals("Error: location Uri doesn't match our expected result", locationUri.toString(),
                "content://in.udacity.learning.shunshine.app/location/1");
    }
        
}

