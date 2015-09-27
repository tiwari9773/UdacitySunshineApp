package in.udacity.learning.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.exception.ValueNotInsertedException;

/**
 * Created by Lokesh on 23-09-2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    /** */
    private String TAG = getClass().getName();

    /** */
    private SQLiteDatabase db;

    /**
     * If want change the database schema, you must increment the database version.
     */
    public static int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "weather2.sqlite";

    private Context myContext;

    public DBHelper(Context context) {
        //What is this SQLiteDatabase.CursorFactory exactly is when is best sciniero to use it
        //SQLiteDatabase.CursorFactory factory = null;

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeatherContract.WeatherEntry.SQL_CREATE);
        db.execSQL(WeatherContract.LocationEntry.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeatherContract.WeatherEntry.SQL_DROP);
        db.execSQL(WeatherContract.LocationEntry.SQL_DROP);

        onCreate(db);
    }

    /* Insert Saved Status of List */
    public long insertInTable(String tableName, String columnHack, ContentValues cv) {
        long rowid = 0;
        try {
            db = this.getWritableDatabase();
            rowid = db.insert(tableName, columnHack, cv);

            if (rowid < 0) {
                throw new ValueNotInsertedException();
            }

        } catch (Exception e) {
            if (AppConstant.DEBUG)
                Log.e(TAG + " insert ", e.toString());
        } finally {
            if (db != null)
                db.close();
        }
        return rowid;
    }
}
