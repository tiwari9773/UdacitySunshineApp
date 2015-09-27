package in.udacity.learning.shunshine.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import in.udacity.learning.dbhelper.DBHelper;
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.shunshine.app.utils.PollingCheck;

/**
 * Created by Lokesh on 27-09-2015.
 */
public class TestUtilities extends AndroidTestCase {

    static final String TEST_LOCATION = "99705";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }


    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + valueCursor.getString(idx) +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        creating the
        LocationEntry part of the WeatherContract.
     */
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.LOCATION_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.CITY_NAME, "North Pole");
        testValues.put(WeatherContract.LocationEntry.CORD_LAT, 64.7488);
        testValues.put(WeatherContract.LocationEntry.CORD_LONG, 147.353);

        return testValues;
    }

    /*
           creating the
           LocationEntry part of the WeatherContract.
        */
    static ContentValues createWeatherValues(long locationRowId) {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();

        testValues.put(WeatherContract.WeatherEntry.DATE, TEST_DATE);
        testValues.put(WeatherContract.WeatherEntry.MIN, 65);
        testValues.put(WeatherContract.WeatherEntry.MAX, 75);
        testValues.put(WeatherContract.WeatherEntry.HUMADITY, 1.2);
        testValues.put(WeatherContract.WeatherEntry.PRESSURE, 1.2);
        testValues.put(WeatherContract.WeatherEntry.LOCATION_KEY, locationRowId);
        testValues.put(WeatherContract.WeatherEntry.DEGREES, 1.1);
        testValues.put(WeatherContract.WeatherEntry.SHORT_DESC, "Asteroids");
        testValues.put(WeatherContract.WeatherEntry.WIND_SPEED, 5.5);
        testValues.put(WeatherContract.WeatherEntry.WEATHER_ID, 321);

        return testValues;
    }

    static long insertNorthPoleLocationValues(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = createNorthPoleLocationValues();

        long locationId = database.insert(WeatherContract.LocationEntry.TABLE_NAME, null, contentValues);
        assertTrue("Fail in Insertion", locationId != -1);

        return locationId;
    }

    /*
       Students: The functions we provide inside of TestProvider use this utility class to test
       the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
       CTS tests.

       Note that this only tests that the onChange function is called; it does not test that the
       correct Uri is returned.
    */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
