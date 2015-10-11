package in.udacity.learning.shunshine.app.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class ForcastFragment extends Fragment implements OnWeatherItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = ForcastFragment.class.getName();

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.DATE,
            WeatherContract.WeatherEntry.SHORT_DESC,
            WeatherContract.WeatherEntry.MAX,
            WeatherContract.WeatherEntry.MIN,
            WeatherContract.LocationEntry.LOCATION_SETTING,
            WeatherContract.WeatherEntry.WEATHER_ID,
            WeatherContract.LocationEntry.CORD_LAT,
            WeatherContract.LocationEntry.CORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    //private WeatherRecycleViewAdapter adapter;
    private ForecastAdapter mForecastAdapter;

    // Unique Loader Id for every loader we create
    private static final int FORECAST_LOADER = 0;
    //private WeatherListAdapter mForecastAdapter;
    private List<String> mItem = new ArrayList<>();

    public ForcastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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


        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.

//        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        lsView.setAdapter(mForecastAdapter);

        lsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                onClickWeather(cursor);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onClickWeather(Cursor cursor) {

        if (cursor != null) {
            cursor.moveToFirst();
            String locationSetting = Utility.getPreferredLocation(getActivity());
            Intent intent = new Intent(getActivity(), DetailActivity.class)
                    .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
            startActivity(intent);
        }
    }


    //method to initiate
    private void updateWeatherApp() {
        if (new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity())) {
            FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
            weatherTask.execute(getSavedKeys());
            //new FetchForcastData().execute(getSavedKeys());
        } else {
            L.lToast(getContext(), getString(R.string.msg_internet_status));
        }
    }

    //Provide value of setting meu
    private String[] getSavedKeys() {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
        String location_setting = s.getString(getString(R.string.pref_keys_zip_code), getString(R.string.pref_location_default));
        String unit = s.getString(getString(R.string.pref_keys_unit_type), getString(R.string.pref_unit_metric));

        return new String[]{location_setting, unit};
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());


        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}