package in.udacity.learning.serviceutility;

import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import in.udacity.learning.keys.JSONKeys;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class JSONParser {

    public static List<String> parseWeatherForcast(String jSonString)
    {
        List<String> str = new ArrayList();

        try {
            JSONObject jsonObject = new JSONObject(jSonString);
            JSONArray jsonArray = jsonObject.getJSONArray(JSONKeys.weatherKeys.LIST);

            //set Time
            Calendar dayTime = new GregorianCalendar();
            Date date =  new Date();
            dayTime.setTime(date);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject origArray = jsonArray.getJSONObject(i);
                JSONObject tempJSON = origArray.getJSONObject(JSONKeys.weatherKeys.TEMP);

                String max = tempJSON.getString(JSONKeys.weatherKeys.MAX);
                String min = tempJSON.getString(JSONKeys.weatherKeys.MIN);

                String min_max = formatHighLows(Double.parseDouble(max), Double.parseDouble(min));
                JSONArray jsonAr = origArray.getJSONArray(JSONKeys.weatherKeys.WEATHER);
                tempJSON = jsonAr.getJSONObject(0);
                String main = tempJSON.getString(JSONKeys.weatherKeys.MAIN);

                //Set date increment
                dayTime.add(Calendar.DATE, i);
                String weatherDate = getReadableDateString(dayTime.getTimeInMillis());

                String format = weatherDate+" - "+main+" - "+min_max;
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
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private static String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

}
