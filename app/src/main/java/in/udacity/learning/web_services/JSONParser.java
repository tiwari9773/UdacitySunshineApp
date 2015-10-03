package in.udacity.learning.web_services;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.shunshine.app.MyApplication;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class JSONParser {

    public static List<String> parseWeatherForcast(String jSonString) {
        List<String> str = new ArrayList();

        try {
            JSONObject jsonObject = new JSONObject(jSonString);
            JSONArray jsonArray = jsonObject.getJSONArray(WebServiceParsingKeys.weatherKeys.LIST);

            //set Time
            Calendar dayTime = new GregorianCalendar();
            Date date = new Date();
            dayTime.setTime(date);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject origArray = jsonArray.getJSONObject(i);
                JSONObject tempJSON = origArray.getJSONObject(WebServiceParsingKeys.weatherKeys.TEMP);

                String max = tempJSON.getString(WebServiceParsingKeys.weatherKeys.MAX);
                String min = tempJSON.getString(WebServiceParsingKeys.weatherKeys.MIN);

                String min_max = formatHighLows(Double.parseDouble(max), Double.parseDouble(min));
                JSONArray jsonAr = origArray.getJSONArray(WebServiceParsingKeys.weatherKeys.WEATHER);
                tempJSON = jsonAr.getJSONObject(0);
                String main = tempJSON.getString(WebServiceParsingKeys.weatherKeys.MAIN);

                //Set date increment
                dayTime.add(Calendar.DATE, i);
                String weatherDate = getReadableDateString(dayTime.getTimeInMillis());

                String format = weatherDate + " - " + main + " - " + min_max;
                str.add(format);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
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

    /**
     * Prepare the weather high/lows for presentation.
     */
    private static String formatHighLows(double high, double low) {


        //If user wants to wee in imerial unit then we will convert it here instead of requery with that unit
        // will be userful when we store data in sqlite

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String unit = sharedPreferences.getString(MyApplication.getContext().getString(R.string.pref_keys_unit_type), MyApplication.getContext().getString(R.string.pref_unit_metric));

        if (unit.equals(MyApplication.getContext().getString(R.string.pref_unit_imperial))) {
            high = high * 1.8 + 32;
            low = low * 1.8 + 32;
        }
//        else
//        {
//            Toast.makeText(MyApplication.getContext(), "Unit type not found", Toast.LENGTH_SHORT).show();
//        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

}
