package in.udacity.learning.shunshine.app.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import in.udacity.learning.adapter.ForecastAdapter;
import in.udacity.learning.adapter.WeatherListAdapter;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.logger.L;
import in.udacity.learning.network.NetWorkInfoUtility;
import in.udacity.learning.shunshine.app.DetailActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.shunshine.app.SettingsActivity;
import in.udacity.learning.utility.Utility;
import in.udacity.learning.web_services.FetchWeatherTask;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForcastFragment extends Fragment implements OnWeatherItemClickListener {

    private final String TAG = ForcastFragment.class.getName();
    /*adapter which holds values*/
    //private WeatherRecycleViewAdapter adapter;
    private ForecastAdapter mForecastAdapter;
    //private WeatherListAdapter mForecastAdapter;
    private List<String> mItem = new ArrayList<>();

    public ForcastFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //If I will remove this "Refresh" menu is apearing twice
        //menu.clear();
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

    @Override
    public void onStart() {
        super.onStart();
        updateWeatherApp();
    }

    public void initialize(View view) {

//        /* Recycle Value holder*/
//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(adapter);

        ListView lsView = (ListView) view.findViewById(R.id.lv_weather_list);

        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//               locationSetting, System.currentTimeMillis());

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.CONTENT_URI;

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        cur.moveToFirst();
        Log.d(TAG, "initialize "+cur.getCount());
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
        lsView.setAdapter(mForecastAdapter);

        lsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onClickWeather(position);
            }
        });
    }

    @Override
    public void onClickWeather(int position) {
        Intent in = new Intent(getActivity(), DetailActivity.class);
        in.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position).toString());
        startActivity(in);
    }

    //method to initiate
    private void updateWeatherApp() {
        if (new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity()))
        {
            FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
            weatherTask.execute(getSavedKeys());
            //new FetchForcastData().execute(getSavedKeys());
        }
        else {
            L.lToast(getContext(), getString(R.string.msg_internet_status));
        }
    }

    //Provide value of setting meu
    private String[] getSavedKeys() {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
        String location_setting = s.getString(getString(R.string.pref_keys_zip_code), getString(R.string.pref_location_default));
        String unit = s.getString(getString(R.string.pref_keys_unit_type), getString(R.string.pref_unit_metric));

        return new String[]{location_setting,unit};
    }
}
