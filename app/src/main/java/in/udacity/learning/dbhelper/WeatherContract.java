package in.udacity.learning.dbhelper;

import android.provider.BaseColumns;

/**
 * Created by Lokesh on 23-09-2015.
 */
public final class WeatherContract {

    public WeatherContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class LocationEntry implements TableContract {
        public static String TABLE_NAME = "location";
        public static String LOCATION_SETTING = "location_setting";
        public static String CITY_NAME = "city_name";
        public static String CORD_LAT = "coord_lat";
        public static String CORD_LONG = "coord_lang";

        public static final String SQL_CREATE = CREATE_TABLE + TABLE_NAME
                + OPEN_BRACE
                + _ID + TYPE_INTEGER + PRIMARY_KEY + AUTO_INCREMENT + SEP_COMMA
                + LOCATION_SETTING + TYPE_TEXT + NOT_NULL + SEP_COMMA
                + CITY_NAME + TYPE_TEXT + NOT_NULL + SEP_COMMA
                + CORD_LAT + TYPE_REAL + NOT_NULL + SEP_COMMA
                + CORD_LONG + TYPE_REAL + NOT_NULL
                + CLOSE_BRACE + SEMICOLON;

        String SQL_DROP = DROP_TABLE + TABLE_NAME;
    }

    /* Inner class that defines the table contents */
    public static abstract class WeatherEntry implements TableContract {
        public static String TABLE_NAME = "weather";
        public static String DATE = "date";
        public static String MIN = "min";
        public static String MAX = "max";
        public static String HUMADITY = "humadity";
        public static String PRESSURE = "pressure";
        public static String LOCATION_ID = "location_id";

        public static final String SQL_CREATE = CREATE_TABLE + TABLE_NAME
                + OPEN_BRACE
                + _ID + TYPE_INTEGER + PRIMARY_KEY + AUTO_INCREMENT + SEP_COMMA
                + DATE + TYPE_REAL + NOT_NULL + SEP_COMMA
                + MIN + TYPE_REAL + NOT_NULL + SEP_COMMA
                + MAX + TYPE_REAL + NOT_NULL + SEP_COMMA
                + HUMADITY + TYPE_REAL + NOT_NULL + SEP_COMMA
                + PRESSURE + TYPE_REAL + NOT_NULL + SEP_COMMA
                + LOCATION_ID + TYPE_INTEGER + NOT_NULL+SEP_COMMA
                + FOREIGN_KEY
                        +OPEN_BRACE+ LOCATION_ID+CLOSE_BRACE+REFERENCES
                        +LocationEntry.TABLE_NAME + OPEN_BRACE+LocationEntry._ID+CLOSE_BRACE
                + CLOSE_BRACE + SEMICOLON;

        String SQL_DROP = DROP_TABLE + TABLE_NAME;
    }
}
