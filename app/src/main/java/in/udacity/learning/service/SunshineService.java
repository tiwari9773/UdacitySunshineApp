package in.udacity.learning.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.shunshine.app.MainActivity;
import in.udacity.learning.utility.Utility;

/**
 * Created by Lokesh on 19-10-2015.
 */
public class SunshineService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = SunshineService.class.getName();

    private GoogleApiClient googleClient;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SunshineService(String name) {
        super(name);
    }

    public SunshineService() {
        super("Sunshine");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Build a new GoogleApiClient
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {

        String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.WEATHER_ID,
                WeatherContract.WeatherEntry.SHORT_DESC,
                WeatherContract.WeatherEntry.MAX,
                WeatherContract.WeatherEntry.MIN
        };
        // these indices must match the projection
        int INDEX_WEATHER_ID = 0;
        int INDEX_SHORT_DESC = 1;
        int INDEX_MAX_TEMP = 2;
        int INDEX_MIN_TEMP = 3;

        // Get today's data from the ContentProvider
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.DATE + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the weather data from the Cursor
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getIconResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        double minTemp = data.getDouble(INDEX_MIN_TEMP);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        String formattedMinTemperature = Utility.formatTemperature(this, minTemp);

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), weatherArtResourceId);
        Asset asset = Utility.getAsset(bitmap);
        data.close();
        sendData(formattedMaxTemperature, formattedMinTemperature, asset, description);
    }

    private void sendData(String max, String min, Asset asset, String desc) {
        // Create a DataMap object and send it to the data layer
        if (googleClient == null || !googleClient.isConnected()) {
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("max", max);
        dataMap.putString("min", min);
        dataMap.putString("desc", desc);
        dataMap.putAsset("thumb", asset);

        Log.i(TAG, "sendData: " + dataMap.toString());
        // Construct a DataRequest and send over the data layer
        PutDataMapRequest putDMR = PutDataMapRequest.create(MainActivity.WEARABLE_DATA_PATH);
        putDMR.getDataMap().putAll(dataMap);
        PutDataRequest request = putDMR.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    Log.v(TAG, "DataMap: " + " sent successfully to data layer ");
                } else {
                    // Log an error
                    Log.v(TAG, "ERROR: failed to send DataMap to data layer");
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: ");
    }
}
