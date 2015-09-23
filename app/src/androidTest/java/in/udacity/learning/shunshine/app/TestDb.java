package in.udacity.learning.shunshine.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import in.udacity.learning.dbhelper.DBHelper;
import in.udacity.learning.dbhelper.WeatherContract;

/**
 * Created by Lokesh on 24-09-2015.
 */
public class TestDb extends AndroidTestCase {

    static final String TEST_LOCATION = "99705";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDatabase() {

    }

    /*
        Students: You can uncomment this helper function once you have finished creating the
        LocationEntry part of the WeatherContract.
     */
    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.LOCATION_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.CITY_NAME, "North Pole");
        testValues.put(WeatherContract.LocationEntry.CORD_LAT, 64.7488);
        testValues.put(WeatherContract.LocationEntry.CORD_LONG, -147.353);

        return testValues;
    }

    static long testInsertNorthPoleLocationValues(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = TestDb.createNorthPoleLocationValues();

        long locationId = database.insert(WeatherContract.WeatherEntry.TABLE_NAME,null,contentValues);
        assertTrue("Fail in Insertion",locationId!=-1);

        return locationId;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
