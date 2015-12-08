package in.udacity.learning.shunshine.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

import in.udacity.learning.dbhelper.DBHelper;
import in.udacity.learning.dbhelper.WeatherContract;

/**
 * Created by Lokesh on 24-09-2015.
 */
public class TestDb extends AndroidTestCase {

    String TAG = TestDb.class.getName();

    /*
       This function gets called before each test is executed to delete the database.  This makes
       sure that we always have a clean test.
    */
    public void setUp() {
        deleteTheDatabase();
    }

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        //If independently we are calling this method then this method will work to clean database
        mContext.deleteDatabase(DBHelper.DATABASE_NAME);

        SQLiteDatabase db = new DBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());


        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.",c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.CORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.CORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.LOCATION_SETTING);

        c.moveToFirst();
            int columnNameIndex = c.getColumnIndex("name");
            do {
                String columnName = c.getString(columnNameIndex);
                locationColumnHashSet.remove(columnName);
                Log.i(TAG, "testCreateDb " + columnName);

            } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }


    /*
     we will build code to test that we can insert and query the
     location database.  You'll want to look in TestUtilities
     where you can uncomment out the "createNorthPoleLocationValues" function.  You can
     also make use of the ValidateCurrentRecord function from within TestUtilities.
 */
    public void testLocationTable() {
        insertLocation();
    }

    public void testWeatherTable() {
        long locationRowId = insertLocation();

        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);


        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        long weatehrRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        assertFalse("Error: Weather Not Inserted Correctly", weatehrRowId == -1L);

// Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );


        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from location query", weatherCursor.moveToFirst());


        // Fifth Step: Validate the location Query
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",weatherCursor, weatherValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from weather query",weatherCursor.moveToNext());

        // Sixth Step: Close cursor and database
        weatherCursor.close();
        dbHelper.close();
    }

    public long insertLocation() {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, contentValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );


        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",cursor, contentValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from location query", cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return locationRowId;

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
