package in.udacity.learning.shunshine.app;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import in.udacity.learning.constantsutility.WebServiceURL;
import in.udacity.learning.logger.L;
import in.udacity.learning.network.NetWorkInfoUtility;

public class MainActivity extends AppCompatActivity {

    /* Holds the value of json response*/
    private String forcastJsonValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
    }

    public void initialize() {
        if (new NetWorkInfoUtility().isNetWorkAvailableNow(this))
            new DownloadValue().execute(WebServiceURL.urlWeatherSevenDayForcast);
        else {
            L.lToast(this, getString(R.string.msg_internet_status));
        }

        /* Recycle Value holder*/
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_frequency_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    class DownloadValue extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader  =null;

            /* Take an URL Object*/
            try {

                URL url = new URL(WebServiceURL.urlWeatherSevenDayForcast);
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

                if(stringBuffer.length()==0)
                {
                    return null;
                }

                forcastJsonValue = stringBuffer.toString();
            } catch (MalformedURLException e) {
                L.lToast(getApplicationContext(), e.toString());
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
                        L.lToast(getApplicationContext(), e.toString());
                    }
            }
            return forcastJsonValue;
        }

        @Override
        protected void onPostExecute(String s) {
            L.lToast(getApplicationContext(),forcastJsonValue);
            super.onPostExecute(s);
        }
    }
}
