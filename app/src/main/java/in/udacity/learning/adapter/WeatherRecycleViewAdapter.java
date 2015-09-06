package in.udacity.learning.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.model.Item;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class WeatherRecycleViewAdapter extends RecyclerView.Adapter<WeatherViewHolder> {

    private List<Item> lsItem;
    private OnWeatherItemClickListener onWeatherItemClickListener;

    public WeatherRecycleViewAdapter(List<Item> lsItem, OnWeatherItemClickListener onWeatherItemClickListener) {
        this.onWeatherItemClickListener = onWeatherItemClickListener;
        this.lsItem = lsItem;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_list, parent, false);
        WeatherViewHolder weatherViewHolder = new WeatherViewHolder(view,onWeatherItemClickListener);

        return weatherViewHolder;
    }


    @Override
    public void onBindViewHolder(WeatherViewHolder holder, int position) {
        holder.tv.setText(getItem(position).getName());
    }

    @Override
    public int getItemCount() {
        return lsItem.size();
    }

    public Item getItem(int position) {
        return lsItem.get(position);
    }

    public void setLsItem(List<Item> lsItem) {
        this.lsItem = lsItem;
    }


}
