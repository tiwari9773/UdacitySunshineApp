package in.udacity.learning.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import in.udacity.learning.model.WeatherAttribute;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 03-10-2015.
 */
public class WeatherListAdapter extends ArrayAdapter {

    private List<String> lsItem;
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
            weatherViewHolder.tv = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(weatherViewHolder);
        } else {
            weatherViewHolder = (ViewHolder) convertView.getTag();
        }

        weatherViewHolder.tv.setText(lsItem.get(position));

        return convertView;
    }

    public void setLsItem(List<String> lsItem) {
        this.lsItem = lsItem;
    }

    public class ViewHolder {
        TextView tv;
    }
}
