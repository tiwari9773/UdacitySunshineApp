package in.udacity.learning.web_services;

/**
 * Created by Lokesh Tiwari on 05-Oct-15.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.dbhelper.WeatherProvider;
import in.udacity.learning.model.LocationAttribute;
import in.udacity.learning.model.WeatherAttribute;
import in.udacity.learning.shunshine.app.MyApplication;
import in.udacity.learning.shunshine.app.R;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG = FetchWeatherTask.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    private static int coutn = 0;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }

    private boolean DEBUG = true;


    @Override
    protected String[] doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        String locationQuery = params[0];
        //By default we are going to call only metric and do conversion here as per need

        String unit = params[1];
        String mode = "json";
        int days = 14;

        String jSonString = new HttpURLConnectionWebService(mode, unit, days, locationQuery).getWeatherJSON(LOG);
        if (jSonString != null) {
            return getWeatherDataFromJson(jSonString, locationQuery);
        }

        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null && mForecastAdapter != null) {
            mForecastAdapter.clear();
            for (String dayForecastStr : result) {
                mForecastAdapter.add(dayForecastStr);
            }
            // New data is back from the server.  Hooray!
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, String locationSetting) {

        try {
            LocationAttribute la = JSONParser.parseLocationForcast(forecastJsonStr);

            long locationId = addLocation(la.getSetting(), la.getCityName(), Double.parseDouble(la.getLati())
                    , Double.parseDouble(la.getLongi()));

            List<WeatherAttribute> lsWeather = JSONParser.parseWeatherForcast(forecastJsonStr, locationId + "");

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(lsWeather.size());
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//
//            for (int i = 0; i < lsWeather.size(); i++) {
//                // These are the values that will be collected.
//
//                WeatherAttribute wa = lsWeather.get(i);
//                ContentValues weatherValues = new ContentValues();
//
//                weatherValues.put(WeatherContract.WeatherEntry.LOCATION_ID, locationId);
//                weatherValues.put(WeatherContract.WeatherEntry.DATE, wa.getWeatherDate());
//                weatherValues.put(WeatherContract.WeatherEntry.HUMADITY, wa.getHumidity());
//                weatherValues.put(WeatherContract.WeatherEntry.PRESSURE, wa.getPressure());
//                weatherValues.put(WeatherContract.WeatherEntry.WIND_SPEED, wa.getWindSpeed());
//                weatherValues.put(WeatherContract.WeatherEntry.DEGREES, wa.getDegree());
//                weatherValues.put(WeatherContract.WeatherEntry.MAX, wa.getMax());
//                weatherValues.put(WeatherContract.WeatherEntry.MIN, wa.getMin());
//                weatherValues.put(WeatherContract.WeatherEntry.SHORT_DESC, wa.getDescription());
//                weatherValues.put(WeatherContract.WeatherEntry.WEATHER_ID, wa.getWeather_id());
//
//                cVVector.add(weatherValues);
//            }
//
//            // add to database
//            if (cVVector.size() > 0) {
//                // Student: call bulkInsert to add the weatherEntries to the database here
//            }
//
//            // Sort order:  Ascending, by date.
//            String sortOrder = WeatherContract.WeatherEntry.DATE + " ASC";
//            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                    locationSetting, System.currentTimeMillis());

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert

//            Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
//                    null, null, null, sortOrder);
//
//            cVVector = new Vector<ContentValues>(cur.getCount());
//            if ( cur.moveToFirst() ) {
//                do {
//                    ContentValues cv = new ContentValues();
//                    DatabaseUtils.cursorRowToContentValues(cur, cv);
//                    cVVector.add(cv);
//                } while (cur.moveToNext());
//            }

            Log.d(LOG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");

            String[] resultStrs = convertContentValuesToUXFormat(cVVector);
            return resultStrs;

        } catch (Exception e) {
            Log.e(LOG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
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
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI

        long locationId = -1;
        ContentValues cv = new ContentValues();
        cv.put(WeatherContract.LocationEntry.CITY_NAME, cityName);
        cv.put(WeatherContract.LocationEntry.CORD_LAT, lat);
        cv.put(WeatherContract.LocationEntry.CORD_LONG, lon);
        cv.put(WeatherContract.LocationEntry.LOCATION_SETTING, locationSetting);

        coutn++;
       // Toast.makeText(mContext, coutn + "", Toast.LENGTH_SHORT).show();
        Cursor c = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, new String[]{WeatherContract.LocationEntry._ID}, WeatherContract.LocationEntry.LOCATION_SETTING + "= ? ", new String[]{locationSetting}, null);

        if (!c.moveToFirst()) {
            Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cv);
            locationId = ContentUris.parseId(uri);
        } else {
            locationId = c.getLong(0);
        }
        if (c != null)
            c.close();

        return locationId;
    }


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // Data is fetched in Celsius by default.
        // If user prefers to see in Fahrenheit, convert the values here.
        // We do this rather than fetching in Fahrenheit so that the user can
        // change this option without us having to re-fetch the data once
        // we start storing the values in a database.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String unitType = sharedPreferences.getString(MyApplication.getContext().getString(R.string.pref_keys_unit_type), MyApplication.getContext().getString(R.string.pref_unit_metric));

        if (unitType.equals(mContext.getString(R.string.pref_unit_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(mContext.getString(R.string.pref_unit_metric))) {
            Log.d(LOG, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for (int i = 0; i < cvv.size(); i++) {
            ContentValues weatherValues = cvv.elementAt(i);
            String highAndLow = formatHighLows(
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.MAX),
                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.MIN));
            resultStrs[i] = getReadableDateString(
                    weatherValues.getAsLong(WeatherContract.WeatherEntry.DATE)) +
                    " - " + weatherValues.getAsString(WeatherContract.WeatherEntry.SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }
}