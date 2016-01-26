package in.udacity.learning.framework;

import android.database.Cursor;

import in.udacity.learning.adapter.ForecastAdapter;

/**
 * Created by Lokesh on 06-09-2015.
 */
public interface OnWeatherItemClickListener {

     void onClickWeather(long date, ForecastAdapter.ForecastAdapterViewHolder vh);
}
