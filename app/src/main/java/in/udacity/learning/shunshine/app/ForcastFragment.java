package in.udacity.learning.shunshine.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import in.udacity.learning.adapter.WeatherRecycleViewAdapter;
import in.udacity.learning.constantsutility.AppConstant;
import in.udacity.learning.constantsutility.WebServiceURL;
import in.udacity.learning.keys.JSONKeys;
import in.udacity.learning.logger.L;
import in.udacity.learning.model.Item;
import in.udacity.learning.network.NetWorkInfoUtility;
import in.udacity.learning.serviceutility.HttpURLConnectionInfo;
import in.udacity.learning.serviceutility.JSONParser;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForcastFragment extends Fragment {

    public ForcastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * I dont know why option menu is apearing twice*
     * pease explain on this ??
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //If I will remove this "Refresh" menu is apearing twice
        menu.clear();
        inflater.inflate(R.menu.forcast_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                if (new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity()))
                    new FetchForcastData().execute("94043");
                else {
                    L.lToast(getContext(), getString(R.string.msg_internet_status));
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initialize(view);
        return view;
    }

    public void initialize(View view) {

        List<Item> itemList = new ArrayList<Item>();
        itemList.add(new Item(1, "ABC"));
        itemList.add(new Item(1, "ABC"));
        itemList.add(new Item(1, "ABC"));
        itemList.add(new Item(1, "ABC"));
        itemList.add(new Item(1, "ABC"));

        /* Recycle Value holder*/
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        WeatherRecycleViewAdapter adapter = new WeatherRecycleViewAdapter(itemList);
        recyclerView.setAdapter(adapter);
    }

    class FetchForcastData extends AsyncTask<String, String, String> {
        String TAG = getClass().getName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String defaultZip = "94043";
            String zip = params.length > 0 ? params[0] : defaultZip;
            String mode = "json";
            String unit = "metric";
            int days = 7;

            String jSonString = new HttpURLConnectionInfo(mode, unit, days, zip).getJSON(TAG);
            String parsedString = JSONParser.parseWeatherForcast(jSonString);
            return parsedString;
        }

        @Override
        protected void onPostExecute(String s) {
            if (AppConstant.DEBUG)
                L.lToast(getContext(), s);

            //Log.v(TAG, s);
            super.onPostExecute(s);
        }
    }
}
