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
import in.udacity.learning.dbhelper.WeatherContract;
import in.udacity.learning.shunshine.app.fragment.DetailFragment;
import in.udacity.learning.shunshine.app.fragment.ForecastFragment;
import in.udacity.learning.logger.L;
import in.udacity.learning.sync.SunshineSyncAdapter;
import in.udacity.learning.utility.Utility;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private Toolbar mToolbar;
    private final String TAG = MainActivity.class.getName();

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private String mLocation;
    private boolean mTwoPane = false;

    public boolean ismTwoPane() {
        return mTwoPane;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);

        initialize(savedInstanceState);

        SunshineSyncAdapter.initializeSyncAdapter(this);
        //Write database to inspect
        //writeDatabase();
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
        getSupportActionBar().setLogo(R.drawable.ic_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("");

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        // Set Todays View should be highlighted or not
        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.frag_container);
        forecastFragment.setmUseTodayLayout(!mTwoPane);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.equals(mLocation)) {
            //ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_ID);
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.frag_container);
            if (ff != null) {
                ff.onLocationChange();
            }

            // try to update Detail Fragment (Works if it is two pane layout)
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (df != null) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void writeDatabase() {
        String path = getApplicationInfo().dataDir + "/databases/" + DBHelper.DATABASE_NAME;
        File dbFile = new File(path);

        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(dbFile);

            File writePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.sqlite");
            if (!writePath.exists())
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

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

}
