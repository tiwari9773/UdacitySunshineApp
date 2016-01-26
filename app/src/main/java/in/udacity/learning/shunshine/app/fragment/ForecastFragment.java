package in.udacity.learning.shunshine.app.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import in.udacity.learning.adapter.ForecastAdapter;
import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.network.NetWorkInfoUtility;
import in.udacity.learning.shunshine.app.MainActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.sync.SunshineSyncAdapter;
import in.udacity.learning.utility.Utility;

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
        public void onItemSelected(Uri dateUri, ForecastAdapter.ForecastAdapterViewHolder vh);
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
    private int mSelectionPostion = RecyclerView.NO_POSITION;

    // key to holds selection position
    private final String POS_KEY = "pos_key";

    //List View which holds list
    private RecyclerView mRecyclerView;

    //Set layout bit if on mobile else small for tablet
    private boolean mUseTodayLayout;

    private boolean mAutoSelectView;
    private int mChoiceMode;
    private boolean mHoldForTransition;

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
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //If I will remove this "Refresh" menu is apearing twice
        inflater.inflate(R.menu.forcast_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_map: {
                openPreferedMapLocation();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.forcast_fragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.forcast_fragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.forcast_fragment_autoSelectView, false);
        mHoldForTransition = a.getBoolean(R.styleable.forcast_fragment_sharedElementTransition, false);
        a.recycle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initialize(view,savedInstanceState);

        return view;
    }

    public void initialize(View view, Bundle savedInstanceState) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.lv_weather_list);
        View emptyView = view.findViewById(R.id.tv_empty_view);

        mForecastAdapter = new ForecastAdapter(this, getActivity(), emptyView, mChoiceMode);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mForecastAdapter);

        final View parallax = view.findViewById(R.id.parallax_bar);
        if (parallax != null) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int max = parallax.getHeight();
                    if (dy > 0) {
                        parallax.setTranslationY(Math.max(-max, parallax.getTranslationY() - dy / 2));
                    } else {
                        parallax.setTranslationY(Math.min(0, parallax.getTranslationY() - dy / 2));
                    }
                }
            });
        }

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(POS_KEY)) {
                // The Recycler View probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.
                mSelectionPostion = savedInstanceState.getInt(POS_KEY);
            }
            mForecastAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }

    public void onLocationChange() {
        updateWeatherApp();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void openPreferedMapLocation() {

        if (mForecastAdapter != null) {
            Cursor c = mForecastAdapter.getCursor();
            if (c != null) {
                c.moveToPosition(0);
                String lat = c.getString(COL_COORD_LAT);
                String longi = c.getString(COL_COORD_LONG);

                //Uri geoLocation = Uri.parse("geo:19.014410,72.847939?").buildUpon().appendQueryParameter("q",location).build();
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri geoLocation = Uri.parse("geo:" + lat + "," + longi);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoLocation);

                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                //mapIntent.setData(geoLocation);

                // Attempt to start an activity that can handle the Intent
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectionPostion != ListView.INVALID_POSITION)
            outState.putInt(POS_KEY, mSelectionPostion);

        mForecastAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onClickWeather(long date, ForecastAdapter.ForecastAdapterViewHolder vh) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, date)
                , vh);
        mSelectionPostion = vh.getAdapterPosition();
    }


    //method to initiate
    private void updateWeatherApp() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    //Provide value of setting menu
    private String[] getSavedKeys() {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getContext());
        String location_setting = s.getString(getString(R.string.pref_keys_location), getString(R.string.pref_location_default));
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

    /*
         Updates the empty list view with contextually relevant information that the user can
         use to determine why they aren't seeing weather.
      */
    private void updateEmptyView() {
        if (mForecastAdapter.getItemCount() == 0) {
            int message = R.string.msg_no_weather_info;
            TextView tv = (TextView) getView().findViewById(R.id.tv_empty_view);
            if (null != tv) {

                @SunshineSyncAdapter.locationStatus int status = Utility.getLocationStatus(getContext());
                switch (status) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.msg_empty_forecast_list_server_error;
                        break;

                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.msg_empty_forecast_list_server_down;
                        break;

                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.msg_empty_forecast_list_invalid_location;
                        break;

                    default:
                        if (!new NetWorkInfoUtility().isNetWorkAvailableNow(getActivity())) {
                            message = R.string.msg_no_internet_;
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mSelectionPostion != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mSelectionPostion);
        }

         /*Update the View*/
        updateEmptyView();

        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int itemPosition = mForecastAdapter.getSelectedItemPosition();
                        if ( RecyclerView.NO_POSITION == itemPosition )
                            itemPosition = 0;

                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(itemPosition);
                        if ( null != vh && mAutoSelectView ) {
                            mForecastAdapter.selectView( vh );
                        }

                        if ( mHoldForTransition ) {
                            getActivity().supportStartPostponedEnterTransition();
                        }
                        return true;
                    }
                  return false;
                  }
                }
            );
        }

        if (AppConstant.DEBUG) {
            Log.d(TAG, "onLoadFinished Position" + mSelectionPostion);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref_keys_location_key_status))) {
                updateEmptyView();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

}
