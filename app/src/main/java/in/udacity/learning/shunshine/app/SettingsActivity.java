package in.udacity.learning.shunshine.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.udacity.learning.fragments.SettingsFragment;

/**
 * Created by Lokesh on 06-09-2015.
 */
public class SettingsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mToolbar = (Toolbar)findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }
}
