package in.udacity.learning.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import java.util.List;
import java.util.Vector;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.model.LocationAttribute;
import in.udacity.learning.model.WeatherAttribute;
import in.udacity.learning.web_services.HttpURLConnectionWebService;
import in.udacity.learning.web_services.JSONParser;

/**
 * Created by Lokesh on 19-10-2015.
 */
public class SunshineService extends IntentService {

    private static String TAG = SunshineService.class.getName();
    public static final String INTENT_LOCATION_QUERY_EXTRA = "lqe";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SunshineService(String name) {
        super(name);
    }

    public SunshineService() {
        super("Sunshine");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String locationQuery = intent.getStringExtra(INTENT_LOCATION_QUERY_EXTRA);

        if (AppConstant.DEBUG)
            Log.d(TAG, "doInBackground 2");

        String unit = "metric";
        String mode = "json";
        int days = 14;

        String jSonString = new HttpURLConnectionWebService(mode, unit, days, locationQuery).getWeatherJSON(TAG);
//        if (jSonString == null) {
//            // return null;
//        }

        getWeatherDataFromJson(jSonString, locationQuery);
        // This will only happen if there was an error getting or parsing the forecast.
        //return null;
    }


    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting) {

        try {
            LocationAttribute la = JSONParser.parseLocationForcast(forecastJsonStr);

            long locationId = addLocation(locationSetting, la.getCityName(), Double.parseDouble(la.getLati())
                    , Double.parseDouble(la.getLongi()));

            List<WeatherAttribute> lsWeather = JSONParser.parseWeatherForcast(forecastJsonStr, locationId + "");

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(lsWeather.size());

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            for (int i = 0; i < lsWeather.size(); i++) {

                // Cheating to convert this to UTC time, which is what we want anyhow
                long dateTime = dayTime.setJulianDay(julianStartDay + i);

                // These are the values that will be collected.

                WeatherAttribute wa = lsWeather.get(i);
                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.LOCATION_ID, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.HUMIDITY, wa.getHumidity());
                weatherValues.put(WeatherContract.WeatherEntry.PRESSURE, wa.getPressure());
                weatherValues.put(WeatherContract.WeatherEntry.WIND_SPEED, wa.getWindSpeed());
                weatherValues.put(WeatherContract.WeatherEntry.DEGREES, wa.getDegree());
                weatherValues.put(WeatherContract.WeatherEntry.MAX, wa.getMax());
                weatherValues.put(WeatherContract.WeatherEntry.MIN, wa.getMin());
                weatherValues.put(WeatherContract.WeatherEntry.SHORT_DESC, wa.getDescription());
                weatherValues.put(WeatherContract.WeatherEntry.WEATHER_ID, wa.getWeather_id());

                cVVector.add(weatherValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cv = new ContentValues[cVVector.size()];
                cVVector.toArray(cv);
                inserted = this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cv);
            }

            if (AppConstant.DEBUG)
                Log.d(TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI

        long locationId = -1;
        Cursor locationCursor = this.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.LOCATION_SETTING + "= ? ",
                new String[]{locationSetting}, null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {

            ContentValues cv = new ContentValues();
            cv.put(WeatherContract.LocationEntry.CITY_NAME, cityName);
            cv.put(WeatherContract.LocationEntry.CORD_LAT, lat);
            cv.put(WeatherContract.LocationEntry.CORD_LONG, lon);
            cv.put(WeatherContract.LocationEntry.LOCATION_SETTING, locationSetting);

            Uri uri = this.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cv);
            locationId = ContentUris.parseId(uri);
        }

        if (locationCursor != null)
            locationCursor.close();

        return locationId;
    }
}
