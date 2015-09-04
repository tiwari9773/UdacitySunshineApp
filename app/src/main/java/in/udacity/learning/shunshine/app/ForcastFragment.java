package in.udacity.learning.shunshine.app;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import in.udacity.learning.network.NetWorkInfoUtility;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForcastFragment extends Fragment {
    String test;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, null);
        initialize(view);
        return view;
    }

    public void initialize(View view) {

        if (new NetWorkInfoUtility().isNetWorkAvailableNow(getContext()))
            new FetchForcastData().execute(WebServiceURL.urlWeatherSevenDayForcast);
        else {
            L.lToast(getContext(), getString(R.string.msg_internet_status));
        }

        /* Recycle Value holder*/
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    class FetchForcastData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

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

                if (stringBuffer.length() == 0) {
                    return null;
                }

                test = stringBuffer.toString();
            } catch (MalformedURLException e) {
                L.lToast(getContext(), e.toString());
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
                        L.lToast(getContext(), e.toString());
                    }
            }
            return test;
        }

        @Override
        protected void onPostExecute(String s) {
            if (AppConstant.DEBUG)
                L.lToast(getContext(), test);
            super.onPostExecute(s);
        }
    }
}
