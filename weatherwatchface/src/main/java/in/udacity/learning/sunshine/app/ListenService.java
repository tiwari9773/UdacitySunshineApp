package in.udacity.learning.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Lokesh on 15-08-2016.
 */
public class ListenService extends WearableListenerService {
    private static final String TAG = ListenService.class.getName();
    public static Asset asset;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        Log.v(TAG, "On dataChange: ");
        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(MyWeatherWatchFace.WEARABLE_DATA_PATH)) {

                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v(TAG, "DataMap received on watch: " + dataMap);

                    Intent in = new Intent();
                    asset = dataMap.getAsset("thumb");

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("max", dataMap.getString("max"));
                    edit.putString("min", dataMap.getString("min"));
                    edit.putString("desc", dataMap.getString("desc"));
                    edit.commit();

                    in.setAction(MyWeatherWatchFace.RECEIVE_ACTION);
                    sendBroadcast(in);
                }

            }
        }
    }
}
