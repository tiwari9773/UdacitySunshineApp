package in.udacity.learning.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView tv;
    View parentView;
    private OnWeatherItemClickListener onWeatherItemClickListener;

    public WeatherViewHolder(View itemView, OnWeatherItemClickListener onWeatherItemClickListener) {
        super(itemView);
        this.onWeatherItemClickListener = onWeatherItemClickListener;
        parentView = itemView.findViewById(R.id.parentPanel);
        parentView.setOnClickListener(this);

        tv = (TextView) itemView.findViewById(R.id.tv_item);
    }

    @Override
    public void onClick(View v) {
        onWeatherItemClickListener.onClickWeather(getLayoutPosition());
    }
}
