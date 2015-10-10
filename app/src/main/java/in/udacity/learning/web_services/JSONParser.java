package in.udacity.learning.web_services;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.model.LocationAttribute;
import in.udacity.learning.model.WeatherAttribute;
import in.udacity.learning.shunshine.app.MyApplication;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class JSONParser {
    private static final String TAG = JSONParser.class.getName();

    public static List<WeatherAttribute> parseWeatherForcast(String jSonString, String locationId) {
        List<WeatherAttribute> lsWA = null;
        try {
            JSONObject jsonObject = new JSONObject(jSonString);
            JSONArray jsonArray = jsonObject.getJSONArray(WebServiceParsingKeys.weatherKeys.LIST);

            // Insert the new weather information into the database
            lsWA = new ArrayList<WeatherAttribute>(jsonArray.length());

            //set Time
            Calendar dayTime = new GregorianCalendar();
            Date date = new Date();
            dayTime.setTime(date);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject origArray = jsonArray.getJSONObject(i);

                String humidity = origArray.getString(WebServiceParsingKeys.weatherKeys.HUMIDITY);
                String pressure = origArray.getString(WebServiceParsingKeys.weatherKeys.PRESSURE);
                String windSpeed = origArray.getString(WebServiceParsingKeys.weatherKeys.SPEED);
                String degree = origArray.getString(WebServiceParsingKeys.weatherKeys.DEGREE);

                JSONObject tempJSON = origArray.getJSONObject(WebServiceParsingKeys.weatherKeys.TEMP);
                String max = tempJSON.getString(WebServiceParsingKeys.weatherKeys.MAX);
                String min = tempJSON.getString(WebServiceParsingKeys.weatherKeys.MIN);

                JSONArray jsonAr = origArray.getJSONArray(WebServiceParsingKeys.weatherKeys.WEATHER);

                JSONObject weatherJSON = jsonAr.getJSONObject(0);
                String weather_id = weatherJSON.getString(WebServiceParsingKeys.weatherKeys.WEATHER_ID);
                String description = weatherJSON.getString(WebServiceParsingKeys.weatherKeys.DESC);

                //Set date increment
                dayTime.add(Calendar.DATE, i);
                String weatherDate = getReadableDateString(dayTime.getTimeInMillis());


                WeatherAttribute wa = new WeatherAttribute(locationId, weatherDate, humidity, pressure, windSpeed, degree, max, min, description, weather_id);
                lsWA.add(wa);

            }

        } catch (JSONException e) {
            Log.e(TAG, "parseWeatherForcast " + " " + e.getMessage() + " " + e.getCause() + " " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return lsWA;
    }

    // parse value of location table
    public static LocationAttribute parseLocationForcast(String jSonString) {
        LocationAttribute la = null;
        try {
            JSONObject jsonObject = new JSONObject(jSonString);
            jsonObject = jsonObject.getJSONObject(WebServiceParsingKeys.locationKeys.CITY);

            //set Time
            Calendar dayTime = new GregorianCalendar();
            Date date = new Date();
            dayTime.setTime(date);


            String id = jsonObject.getString(WebServiceParsingKeys.locationKeys.ID);
            String city_name = jsonObject.getString(WebServiceParsingKeys.locationKeys.CITY_NAME);

            jsonObject = jsonObject.getJSONObject(WebServiceParsingKeys.locationKeys.COORD);

            String lat = jsonObject.getString(WebServiceParsingKeys.locationKeys.LATI);
            String lon = jsonObject.getString(WebServiceParsingKeys.locationKeys.LONGI);

            la = new LocationAttribute(id, city_name, lat, lon);

        } catch (JSONException e) {
            Log.e(TAG, "parseLocationForcast " + " " + e.getMessage() + " " + e.getCause() + " " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return la;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
            * so for convenience we're breaking it out into its own method now.
            */
    private static String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

}
