package in.udacity.learning.shunshine.app.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import in.udacity.learning.adapter.WeatherListAdapter;
import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.logger.L;
import in.udacity.learning.model.Item;
import in.udacity.learning.network.NetWorkInfoUtility;
import in.udacity.learning.shunshine.app.DetailActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.shunshine.app.SettingsActivity;
import in.udacity.learning.web_services.FetchWeatherTask;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class ForcastFragment extends Fragment implements OnWeatherItemClickListener {

    /*adapter which holds values*/
    //private WeatherRecycleViewAdapter adapter;
    private WeatherListAdapter mForecastAdapter;
    private List<Item> mItem = new ArrayList<>();

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
       // updateWeatherApp();
    }

    public void initialize(View view) {

//        /* Recycle Value holder*/
//        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_frequency_list);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setAdapter(adapter);

        ListView lsView = (ListView) view.findViewById(R.id.lv_weather_list);
        mForecastAdapter = new WeatherListAdapter(getActivity(), R.layout.item_weather_list, R.id.tv_item, mItem);
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
            FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
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
        String zip = s.getString(getString(R.string.pref_keys_zip_code), getString(R.string.pref_city_zip));
        String unit = s.getString(getString(R.string.pref_keys_unit_type), getString(R.string.pref_unit_metric));

        return new String[]{zip,unit};
    }
}
