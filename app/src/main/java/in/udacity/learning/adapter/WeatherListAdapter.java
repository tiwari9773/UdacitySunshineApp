package in.udacity.learning.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import in.udacity.learning.framework.OnWeatherItemClickListener;
import in.udacity.learning.model.Item;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 03-10-2015.
 */
public class WeatherListAdapter extends ArrayAdapter {

    private List<Item> lsItem;
    private Context context;

    public WeatherListAdapter(Context context, int resource, int textViewResourceId, List objects) {
        super(context, resource, textViewResourceId, objects);
        lsItem = objects;
        this.context = context;

    }


    @Override
    public int getCount() {
        return lsItem.size();
    }

    @Override
    public Object getItem(int position) {
        return lsItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder weatherViewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_weather_list, parent, false);
            weatherViewHolder = new ViewHolder();
            weatherViewHolder.tv = (TextView) convertView.findViewById(R.id.tv_item);
            convertView.setTag(weatherViewHolder);
        } else {
            weatherViewHolder = (ViewHolder) convertView.getTag();
        }

        weatherViewHolder.tv.setText(lsItem.get(position).getName());

        return convertView;
    }

    public void setLsItem(List<Item> lsItem) {
        this.lsItem = lsItem;
    }

    public class ViewHolder {
        TextView tv;
    }
}
