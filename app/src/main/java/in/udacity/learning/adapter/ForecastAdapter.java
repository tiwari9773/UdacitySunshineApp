package in.udacity.learning.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.shunshine.app.MainActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.shunshine.app.fragment.ForecastFragment;
import in.udacity.learning.utility.Utility;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    final private Context mContext;

    private OnWeatherItemClickListener onWeatherItemClickListener;

    private View emptyView;
    final private ItemChoiceManager mICM;

    public void setmUseTodayLayout(boolean mUseTodayLayout) {
        this.mUseTodayLayout = mUseTodayLayout;
    }

    public ForecastAdapter(OnWeatherItemClickListener onWeatherItemClickListener, Context context, View emptyView, int choiceMode) {
        this.onWeatherItemClickListener = onWeatherItemClickListener;
        mContext = context;
        this.emptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    /* Remember that these views are reused as needed. */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.item_today_weather;
                    break;
                }
                case VIEW_TYPE_FUTURE_DAY: {
                    layoutId = R.layout.item_weather_list;
                    break;
                }
            }

            View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new ForecastAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        int viewType = getItemViewType(position);
        int weatherId = mCursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                fallbackIconId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
            }
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(forecastAdapterViewHolder.iconView);

        ViewCompat.setTransitionName(forecastAdapterViewHolder.iconView, "iconView" + position);

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable
        // viewHolder.iconView.setContentDescription(cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        //Read Date

        ForecastAdapterViewHolder viewHolder = forecastAdapterViewHolder;
        long dayInMillis = mCursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dayInMillis));

        String description = mCursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(description);
        viewHolder.descriptionView.setContentDescription(mContext.getString(R.string.a11y_forecast, description));

        // Read user preference for metric or imperial temperature units
        //boolean isMetric = Utility.isMetric(context);

        double low = mCursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String strLow = Utility.formatTemperature(mContext, low);
        viewHolder.lowTempView.setText(strLow);
        viewHolder.lowTempView.setContentDescription(mContext.getString(R.string.a11y_low_temp, strLow));

        // Read high temperature from cursor
        double high = mCursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String strHigh = Utility.formatTemperature(mContext, high);
        viewHolder.highTempView.setText(strHigh);
        viewHolder.highTempView.setContentDescription(mContext.getString(R.string.a11y_high_temp, strHigh));

        mICM.onBindViewHolder(forecastAdapterViewHolder, position);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }


    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }


    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ForecastAdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.iv_weather_thumbnail);
            dateView = (TextView) view.findViewById(R.id.tv_date);
            descriptionView = (TextView) view.findViewById(R.id.tv_weather_desc);
            lowTempView = (TextView) view.findViewById(R.id.tv_min_temp);
            highTempView = (TextView) view.findViewById(R.id.tv_max_temp);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            long date = mCursor.getLong(mCursor.getColumnIndex(WeatherContract.WeatherEntry.DATE));
            onWeatherItemClickListener.onClickWeather(date, ForecastAdapterViewHolder.this);
            mICM.onClick(ForecastAdapterViewHolder.this);
        }
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof ForecastAdapterViewHolder ) {
            ForecastAdapterViewHolder vfh = (ForecastAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }

    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
