package in.udacity.learning.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.model.LocationAttribute;
import in.udacity.learning.model.WeatherAttribute;
import in.udacity.learning.shunshine.app.BuildConfig;
import in.udacity.learning.shunshine.app.MainActivity;
import in.udacity.learning.shunshine.app.R;
import in.udacity.learning.utility.Utility;
import in.udacity.learning.web_services.JSONParser;
import in.udacity.learning.web_services.WebServiceURL;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String TAG = SunshineSyncAdapter.class.getSimpleName();

    public static final String ACTION_DATA_UPDATED =
            "in.udacity.learning.shunshine.app.ACTION_DATA_UPDATED";


    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.WEATHER_ID,
            WeatherContract.WeatherEntry.MAX,
            WeatherContract.WeatherEntry.MIN,
            WeatherContract.WeatherEntry.SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    /*Replacement of enum as annotated constant*/
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID, LOCATION_STATUS_INVALID, LOCATION_STATUS_UNKNOWN})
    public @interface locationStatus {
    }

    /*Annotation mode for network status*/
    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_INVALID = 3;
    public static final int LOCATION_STATUS_UNKNOWN = 4;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        if (AppConstant.DEVELOPER_TRACK)
            Log.d(TAG, "Starting sync");


        // We no longer need just the location String, but also potentially the latitude and
        // longitude, in case we are syncing based on a new Place Picker API result.
        Context context = getContext();
        String locationQuery = Utility.getPreferredLocation(context);
        String locationLatitude = String.valueOf(Utility.getLocationLatitude(context));
        String locationLongitude = String.valueOf(Utility.getLocationLongitude(context));

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = getContext().getResources().getString(R.string.config_default_format);
        String units = getContext().getResources().getString(R.string.config_default_unit_type);
        int numDays = Integer.parseInt(getContext().getResources().getString(R.string.config_default_days));

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL = WebServiceURL.baseURLWeatherForcast;

            Uri.Builder uriBuilder = Uri.parse(FORECAST_BASE_URL).buildUpon();

            if (Utility.isLocationLatLonAvailable(context)) {
                uriBuilder.appendQueryParameter(WebServiceURL.LAT_PARAM, locationLatitude)
                        .appendQueryParameter(WebServiceURL.LON_PARAM, locationLongitude);
            } else {
                uriBuilder.appendQueryParameter(WebServiceURL.QUERY, locationQuery);
            }

            uriBuilder.appendQueryParameter(WebServiceURL.MODE, format)
                    .appendQueryParameter(WebServiceURL.UNIT, units)
                    .appendQueryParameter(WebServiceURL.DAYS, Integer.toString(numDays))
                    .appendQueryParameter(WebServiceURL.KEYS, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();

            URL url = new URL(uriBuilder.build().toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setsLocationPreference(getContext(), LOCATION_STATUS_SERVER_DOWN);
                return;
            }
            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr, locationQuery);

            /*Close input-stream-reader*/
            inputStream.close();
        } catch (IOException e) {
            setsLocationPreference(getContext(), LOCATION_STATUS_SERVER_DOWN);
            Log.e(TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            setsLocationPreference(getContext(), LOCATION_STATUS_SERVER_INVALID);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting) throws JSONException {

        try {
            LocationAttribute la = JSONParser.parseLocationForcast(forecastJsonStr);

            if (la != null) {
                long locationId = addLocation(locationSetting, la.getCityName(), Double.parseDouble(la.getLati())
                        , Double.parseDouble(la.getLongi()));

                List<WeatherAttribute> lsWeather = JSONParser.parseWeatherForcast(forecastJsonStr, locationId + "");

                // Insert the new weather information into the database
                Vector<ContentValues> cVVector = new Vector<ContentValues>(lsWeather.size());

                // OWM returns daily forecasts based upon the local time of the city that is being
                // asked for, which means that we need to know the GMT offset to translate this data
                // properly.

                // Since this data is also sent in-order and the first day is always the
                // current day, we're going to take advantage of that to get a nice
                // normalized UTC date for all of our weather.

                Time dayTime = new Time();
                dayTime.setToNow();

                // we start at the day returned by local time. Otherwise this is a mess.
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                // now we work exclusively in UTC
                dayTime = new Time();

                for (int i = 0; i < lsWeather.size(); i++) {

                    // Cheating to convert this to UTC time, which is what we want anyhow
                    long dateTime = dayTime.setJulianDay(julianStartDay + i);

                    // These are the values that will be collected.

                    WeatherAttribute wa = lsWeather.get(i);
                    ContentValues weatherValues = new ContentValues();

                    weatherValues.put(WeatherContract.WeatherEntry.LOCATION_ID, locationId);
                    weatherValues.put(WeatherContract.WeatherEntry.DATE, dateTime);
                    weatherValues.put(WeatherContract.WeatherEntry.HUMIDITY, wa.getHumidity());
                    weatherValues.put(WeatherContract.WeatherEntry.PRESSURE, wa.getPressure());
                    weatherValues.put(WeatherContract.WeatherEntry.WIND_SPEED, wa.getWindSpeed());
                    weatherValues.put(WeatherContract.WeatherEntry.DEGREES, wa.getDegree());
                    weatherValues.put(WeatherContract.WeatherEntry.MAX, wa.getMax());
                    weatherValues.put(WeatherContract.WeatherEntry.MIN, wa.getMin());
                    weatherValues.put(WeatherContract.WeatherEntry.SHORT_DESC, wa.getDescription());
                    weatherValues.put(WeatherContract.WeatherEntry.WEATHER_ID, wa.getWeather_id());

                    cVVector.add(weatherValues);
                }

                int inserted = 0;
                // add to database
                if (cVVector.size() > 0) {
                    ContentValues[] cv = new ContentValues[cVVector.size()];
                    cVVector.toArray(cv);
                    inserted = getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cv);

                    if (AppConstant.DEVELOPER_TRACK)
                        Log.d(TAG, "getWeatherDataFromJson, Data Inserted" + inserted);

                    // Delete Previous Days Data
                    String where = WeatherContract.WeatherEntry.DATE + "<=?";
                    String values[] = {Long.toString(dayTime.setJulianDay(julianStartDay - 1))};
                    long countDeleted = getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI, where, values);
                    if (AppConstant.DEVELOPER_TRACK)
                        Log.d(TAG, "getWeatherDataFromJson, Data Deleted" + countDeleted);

                    updateWidgets();
                    notifyWeather();
                }

                if (AppConstant.DEBUG)
                    Log.d(TAG, TAG + " Complete. " + inserted + " Inserted");
            } else {
                  /*Till System detects Location is valid or not, mark it as unknown*/
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//                EditTextPreference editTextPreference  = (EditTextPreference);
//                editTextPreference.setTitle(getString(R.string.pref_location_unknown_description));
//                updateEmptyView();
            }

        } catch (JSONException e) {
            if (AppConstant.DEVELOPER_TRACK)
                Log.e(TAG, e.getMessage(), e);
            e.printStackTrace();
            throw e;
        }
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI

        long locationId = -1;
        Cursor locationCursor = getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.LOCATION_SETTING + "= ? ",
                new String[]{locationSetting}, null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {

            ContentValues cv = new ContentValues();
            cv.put(WeatherContract.LocationEntry.CITY_NAME, cityName);
            cv.put(WeatherContract.LocationEntry.CORD_LAT, lat);
            cv.put(WeatherContract.LocationEntry.CORD_LONG, lon);
            cv.put(WeatherContract.LocationEntry.LOCATION_SETTING, locationSetting);

            Uri uri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cv);
            locationId = ContentUris.parseId(uri);
        }

        if (locationCursor != null)
            locationCursor.close();

        return locationId;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        if (AppConstant.DEVELOPER_TRACK) {
            Log.d("LOG", "syncImmediately Called.");
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_keys_notification);
        boolean isNotificationEnabled = prefs.getBoolean(key,
                Boolean.parseBoolean(context.getString(R.string.pref_notification_value_default)));

        if (isNotificationEnabled) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (AppConstant.DEVELOPER)
                Log.i(TAG, (System.currentTimeMillis() - lastSync) + " ---- " + DAY_IN_MILLIS);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.
                String locationQuery = Utility.getPreferredLocation(context);

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                    Resources resources = context.getResources();
                    int artResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
                    String artUrl = Utility.getArtUrlForWeatherCondition(context, weatherId);

                    // On Honeycomb and higher devices, we can retrieve the size of the large icon
                    // Prior to that, we use a fixed size
                    @SuppressLint("InlinedApi")
                    int largeIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);
                    @SuppressLint("InlinedApi")
                    int largeIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                            ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                            : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                    // Retrieve the large icon
                    Bitmap largeIcon;
                    try {
                        largeIcon = Glide.with(context)
                                .load(artUrl)
                                .asBitmap()
                                .error(artResourceId)
                                .fitCenter()
                                .into(largeIconWidth, largeIconHeight).get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, "Error retrieving large icon from " + artUrl, e);
                        largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
                    }

                    String title = context.getString(R.string.app_name);

                    // Define the text of the forecast.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            desc,
                            Utility.formatTemperature(context, high),
                            Utility.formatTemperature(context, low));

                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
                    // notifications.  Just throw in some data.
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setColor(resources.getColor(R.color.primary_light))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setContentTitle(title)
                                    .setContentText(contentText);


                    Intent in = new Intent(context, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(in);

                    //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pendingIntent);

                    //Pending Intent for notification click
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }

    /*Sets the location preference into shared preference */
    public static void setsLocationPreference(Context context, @locationStatus int location_status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putInt(context.getString(R.string.pref_keys_location_key_status), location_status);
        spEdit.commit();
    }
}