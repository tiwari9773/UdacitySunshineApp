package in.udacity.learning.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import in.udacity.learning.adapter.WeatherRecycleViewAdapter;
import in.udacity.learning.constantsutility.AppConstant;
import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.logger.L;
import in.udacity.learning.model.Item;
import in.udacity.learning.network.NetWorkInfoUtility;
import in.udacity.learning.serviceutility.HttpURLConnectionInfo;
import in.udacity.learning.serviceutility.JSONParser;
import in.udacity.learning.shunshine.app.DetailActivity;
import in.udacity.learning.shunshine.app.MyApplication;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.shunshine.app.SettingsActivity;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForcastFragment extends Fragment implements OnWeatherItemClickListener {

    /*adapter which holds values*/
    private WeatherRecycleViewAdapter adapter;
    private List<Item> mItem = new ArrayList<>();

    public ForcastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeatherApp();
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
                updateWeatherApp();
            }
            break;
            case R.id.action_settings: {
                Intent in = new Intent(getActivity(), SettingsActivity.class);
                startActivity(in);
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

        /* Recycle Value holder*/
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new WeatherRecycleViewAdapter(mItem, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickWeather(int position) {
        Intent in = new Intent(getActivity(), DetailActivity.class);
        in.putExtra(Intent.EXTRA_TEXT, adapter.getItem(position).toString());
        startActivity(in);

    }

    //method to initiate
    private void updateWeatherApp() {
        if (new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity()))

            new FetchForcastData().execute(getSavedKeys());
        else {
            L.lToast(getContext(), getString(R.string.msg_internet_status));
        }
    }

    //Provide value of setting meu
    private String[] getSavedKeys() {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
        String unit = s.getString(getString(R.string.pref_keys_list), "metric");
        String zip = s.getString(getString(R.string.pref_keys_edit), "94043");

        return new String[]{unit, zip};
    }


    class FetchForcastData extends AsyncTask<String, String, List<String>> {
        String TAG = getClass().getName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(String... params) {

            String unit = params[0];
            String zip = params[1];
            String mode = "json";
            int days = 7;

            String jSonString = new HttpURLConnectionInfo(mode, unit, days, zip).getJSON(TAG);
            List<String> parsedString = JSONParser.parseWeatherForcast(jSonString);
            return parsedString;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            if (AppConstant.DEBUG)
                L.lToast(getContext(), s.toString());

            mItem = new ArrayList<>();

            int count = 1;
            for (String temp : s) {
                mItem.add(new Item(count++, temp));
            }
            if (mItem.size() > 0 && adapter!=null) {
                adapter.setLsItem(mItem);
                adapter.notifyDataSetChanged();
            }

            super.onPostExecute(s);
        }
    }
}
