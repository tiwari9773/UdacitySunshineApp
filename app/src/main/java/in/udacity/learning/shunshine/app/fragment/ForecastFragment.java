package in.udacity.learning.shunshine.app.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import in.udacity.learning.service.SunshineService;
import in.udacity.learning.shunshine.app.DetailActivity;
import in.udacity.learning.shunshine.app.MainActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.shunshine.app.SettingsActivity;
import in.udacity.learning.utility.Utility;
import in.udacity.learning.web_services.FetchWeatherTask;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForecastFragment extends Fragment implements OnWeatherItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = ForecastFragment.class.getName();

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

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

    // Holds the clicked position of Item, remember to make at that place list when user rotates
    private int mSelectionPostion = -1;

    // key to holds selectiom position
    private final String POS_KEY = "pos_key";

    //List View which holds list
    private ListView mlsView;

    //Set layout bit if on mobile else small for tablet
    private boolean mUseTodayLayout;

    public ForecastFragment() {
    }

    public void setmUseTodayLayout(boolean mUseTodayLayout) {
        this.mUseTodayLayout = mUseTodayLayout;
        if (mForecastAdapter != null)
            mForecastAdapter.setmUseTodayLayout(this.mUseTodayLayout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //If I will remove this "Refresh" menu is apearing twice
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
        if (savedInstanceState != null && savedInstanceState.containsKey(POS_KEY))
            mSelectionPostion = savedInstanceState.getInt(POS_KEY);

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initialize(view);


        return view;
    }

    public void onLocationChange() {
        updateWeatherApp();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    public void initialize(View view) {

//        /* Recycle Value holder*/
//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(adapter);

        mlsView = (ListView) view.findViewById(R.id.lv_weather_list);


        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mlsView.setAdapter(mForecastAdapter);

        mlsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectionPostion = position;
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                onClickWeather(cursor);
            }
        });

        // Lets keep first Item Selected if it is tablet
        if (((MainActivity) getActivity()).ismTwoPane())
            mlsView.setSelection(0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectionPostion != ListView.INVALID_POSITION)
            outState.putInt(POS_KEY, mSelectionPostion);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClickWeather(Cursor cursor) {

        if (cursor != null) {
            String locationSetting = Utility.getPreferredLocation(getActivity());
            ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
            ));
        }
    }


    //method to initiate
    private void updateWeatherApp() {
        if (new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity())) {
            // The whole thing is replaced by SunshineSErvice
            //FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
            //weatherTask.execute(getSavedKeys());


            Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
            alarmIntent.putExtra(SunshineService.INTENT_LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getContext()));
            PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pi);

            // Using Intent Service
            Intent in = new Intent(getActivity(), SunshineService.class);
            in.putExtra(SunshineService.INTENT_LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getContext()));
            getActivity().startService(in);


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
        if (mSelectionPostion != ListView.INVALID_POSITION)
            mlsView.setSelection(mSelectionPostion);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
