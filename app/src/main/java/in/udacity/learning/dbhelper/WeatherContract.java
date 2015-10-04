package in.udacity.learning.dbhelper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Lokesh on 23-09-2015.
 */
public final class WeatherContract {

    private final String TAG = WeatherContract.class.getName();

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "in.udacity.learning.shunshine.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data.

    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";


    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);

    }

    public WeatherContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;


        public static String TABLE_NAME = "location";
        public static String LOCATION_SETTING = "location_setting";
        public static String CITY_NAME = "city_name";
        public static String CORD_LAT = "coord_lat";
        public static String CORD_LONG = "coord_lang";

        public static final String SQL_CREATE = T.CREATE_TABLE + TABLE_NAME
                + T.OPEN_BRACE
                + _ID + T.TYPE_INTEGER + T.PRIMARY_KEY + T.AUTO_INCREMENT + T.SEP_COMMA
                + LOCATION_SETTING + T.TYPE_TEXT + T.NOT_NULL + T.SEP_COMMA
                + CITY_NAME + T.TYPE_TEXT + T.NOT_NULL + T.SEP_COMMA
                + CORD_LAT + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + CORD_LONG + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + T.UNIQUE + T.OPEN_BRACE + LOCATION_SETTING + T.CLOSE_BRACE + T.ON_CONFLICT_REPLACE
                + T.CLOSE_BRACE + T.SEMICOLON;

        public static final String SQL_DROP = T.DROP_TABLE + TABLE_NAME;

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents */
    public static abstract class WeatherEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_BASE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static String TABLE_NAME = "weather";
        public static String LOCATION_ID = "location_id";
        public static String DATE = "date";
        public static String SHORT_DESC = "short_desc";
        public static String WEATHER_ID = "weather_id";

        public static String MIN = "min";
        public static String MAX = "max";

        public static String HUMADITY = "humadity";
        public static String PRESSURE = "pressure";
        public static String WIND_SPEED = "wind_speed";
        public static String DEGREES = "degree";

        public static final String SQL_CREATE = T.CREATE_TABLE + TABLE_NAME
                + T.OPEN_BRACE
                + _ID + T.TYPE_INTEGER + T.PRIMARY_KEY + T.AUTO_INCREMENT + T.SEP_COMMA
                + LOCATION_ID + T.TYPE_INTEGER + T.NOT_NULL + T.SEP_COMMA
                + DATE + T.TYPE_INTEGER + T.NOT_NULL + T.SEP_COMMA
                + SHORT_DESC + T.TYPE_TEXT + T.NOT_NULL + T.SEP_COMMA
                + WEATHER_ID + T.TYPE_INTEGER + T.NOT_NULL + T.SEP_COMMA

                + MIN + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + MAX + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA

                + HUMADITY + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + PRESSURE + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + WIND_SPEED + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA
                + DEGREES + T.TYPE_REAL + T.NOT_NULL + T.SEP_COMMA

                + T.FOREIGN_KEY
                + T.OPEN_BRACE + LOCATION_ID + T.CLOSE_BRACE + T.REFERENCES
                + LocationEntry.TABLE_NAME + T.OPEN_BRACE + LocationEntry._ID + T.CLOSE_BRACE + T.SEP_COMMA
                + T.UNIQUE + T.OPEN_BRACE + DATE + T.SEP_COMMA + LOCATION_ID + T.CLOSE_BRACE + T.ON_CONFLICT_REPLACE
                + T.CLOSE_BRACE + T.SEMICOLON;

        public static final String SQL_DROP = T.DROP_TABLE + TABLE_NAME;

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /*
           This is the buildWeatherLocation function you filled in.
         */
        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }


        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
