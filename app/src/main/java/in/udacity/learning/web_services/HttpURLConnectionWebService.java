package in.udacity.learning.web_services;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.keys.Keys;
import in.udacity.learning.logger.L;
import in.udacity.learning.shunshine.app.BuildConfig;
import in.udacity.learning.shunshine.app.MyApplication;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class HttpURLConnectionWebService {

    String mode = "json";
    String unit = "metric";
    int days = 14;
    String location_setting = "94043";


    public HttpURLConnectionWebService(String mode, String unit, int days, String location_setting) {
        this.mode = mode;
        this.unit = unit;
        this.days = days;

        // If location_setting is supplied correctly use current one else default one
        if (location_setting.length() > 0)
            this.location_setting = location_setting;
    }

    public String getWeatherJSON(String TAG) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
         /* Take an URL Object*/
        try {

            Uri builtUri = Uri.parse(WebServiceURL.baseURLWeatherForcast).buildUpon()
                    .appendQueryParameter(WebServiceURL.QUERY, location_setting)
                    .appendQueryParameter(WebServiceURL.MODE, mode)
                    .appendQueryParameter(WebServiceURL.UNIT, unit)
                    .appendQueryParameter(WebServiceURL.DAYS, Integer.toString(days))
                    .appendQueryParameter(WebServiceURL.KEYS, BuildConfig.OPEN_WEATHER_MAP_API_KEY).build();

            URL url = new URL(builtUri.toString());

                /* */
            if (AppConstant.DEVELOPER)
                Log.v(TAG, builtUri.toString());

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();

            if (inputStream == null) {
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + " " + "\\n");
            }

            if (stringBuffer.length() == 0) {
                return null;
            }

            return stringBuffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();

            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
        }
        return null;
    }
}
