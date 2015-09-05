package in.udacity.learning.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 05-09-2015.
 */
public class WeatherViewHolder extends RecyclerView.ViewHolder {
    TextView tv;

    public WeatherViewHolder(View itemView) {
        super(itemView);

        tv = (TextView)itemView.findViewById(R.id.tv_item);

    }
}
