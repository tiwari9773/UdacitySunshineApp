package in.udacity.learning.serviceutility;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import in.udacity.learning.constantsutility.AppConstant;
import in.udacity.learning.constantsutility.WebServiceURL;
import in.udacity.learning.logger.L;
import in.udacity.learning.shunshine.app.MyApplication;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class HttpURLConnectionInfo {

    String defaultZip = "94043";
    String mode = "json";
    String unit = "metric";
    int days = 7;
    String zip = "";


    public HttpURLConnectionInfo(String mode, String unit, int days, String zip) {
        this.mode = mode;
        this.unit = unit;
        this.days = days;
        this.zip = zip.length() > 0 ? zip : defaultZip;
    }

    public String getJSON(String TAG) {
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
         /* Take an URL Object*/
        try {

            Uri builtUri = Uri.parse(WebServiceURL.baseURLWeatherForcast).buildUpon()
                    .appendQueryParameter(WebServiceURL.QUERY, zip)
                    .appendQueryParameter(WebServiceURL.MODE, mode)
                    .appendQueryParameter(WebServiceURL.UNIT, unit)
                    .appendQueryParameter(WebServiceURL.DAYS, Integer.toString(days)).build();
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
            L.lToast(MyApplication.getContext(), e.toString());
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();

            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (final Exception e) {
                    L.lToast(MyApplication.getContext(), e.toString());
                }
        }
        return null;
    }
}
