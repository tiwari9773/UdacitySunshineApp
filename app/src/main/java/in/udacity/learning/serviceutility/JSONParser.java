package in.udacity.learning.serviceutility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import in.udacity.learning.keys.JSONKeys;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class JSONParser {

    public static String parseWeatherForcast(String jSonString)
    {
        StringBuffer buf = new StringBuffer();
        try {
            JSONObject jsonObject = new JSONObject(jSonString);
            JSONArray jsonArray = jsonObject.getJSONArray(JSONKeys.weatherKeys.LIST);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject original = jsonArray.getJSONObject(i);
                JSONObject temp = original.getJSONObject(JSONKeys.weatherKeys.TEMP);

                String max = temp.getString(JSONKeys.weatherKeys.MAX);
                String min = temp.getString(JSONKeys.weatherKeys.MIN);

                JSONArray jsonAr = original.getJSONArray(JSONKeys.weatherKeys.WEATHER);
                temp = jsonAr.getJSONObject(0);
                String main = temp.getString(JSONKeys.weatherKeys.MAIN);
                buf.append(min+" ").append(max+" ").append(main).append("\n");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }


}
