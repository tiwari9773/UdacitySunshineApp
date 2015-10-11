package in.udacity.learning.shunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import in.udacity.learning.dbhelper.DBHelper;
import in.udacity.learning.shunshine.app.fragment.ForcastFragment;
import in.udacity.learning.logger.L;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize(savedInstanceState);

        //Write database to inspect
       writeDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_map) {
            openPreferedMapLocation();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferedMapLocation() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        //Uri geoLocation = Uri.parse("geo:19.014410,72.847939?").buildUpon().appendQueryParameter("q",location).build();
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri geoLocation = Uri.parse("geo:19.014410,72.847939");

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoLocation);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");
        //mapIntent.setData(geoLocation);

        // Attempt to start an activity that can handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    //
    public void initialize(Bundle savedInstanceState) {

        //setup toolbar to have more control over Actionbar /** Kindly guide on this
        // is this good approach
        // 1. When to use inbuild Actionbar?
        // 2. When to use toolbar ?*/
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container, new ForcastFragment()).commit();
        } else {
            L.lToast(getBaseContext(), "Test");
        }
    }

    private void writeDatabase() {
        String path = getApplicationInfo().dataDir + "/databases/" + DBHelper.DATABASE_NAME;
        File dbFile = new File(path);

        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(dbFile);

            File writePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.sqlite");
            if(!writePath.exists())
                writePath.createNewFile();
            os = new FileOutputStream(writePath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "writeDatabase " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "writeDatabase " + e.toString());
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "writeDatabase " + e.toString());
                }

            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "writeDatabase " + e.toString());
                }
        }


    }
}
