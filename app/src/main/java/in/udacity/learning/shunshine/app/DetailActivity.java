package in.udacity.learning.shunshine.app;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.shunshine.app.fragment.DetailFragment;

public class DetailActivity extends AppCompatActivity {
    private DetailFragment mDetailFragment;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState) {
        mToolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent in = getIntent();
        String weatherRep = "";
        if (in != null) {
            weatherRep = in.getDataString();
        }

        if (savedInstanceState == null) {
            Bundle b = new Bundle();
            b.putString(Intent.EXTRA_TEXT, weatherRep);
            mDetailFragment = new DetailFragment();
            mDetailFragment.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mDetailFragment)
                    .commit();
        }
    }

}
