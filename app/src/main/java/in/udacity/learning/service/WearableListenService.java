package in.udacity.learning.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Lokesh on 16-08-2016.
 */
public class WearableListenService extends WearableListenerService {

    private static final String TAG = WearableListenService.class.getName();

    private String signal = "/signal";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            Log.i(TAG, "onDataChanged: ");
            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(signal)) {
                    Intent in = new Intent(this, SunshineService.class);
                    startService(in);
                }
            }
        }
    }
}
