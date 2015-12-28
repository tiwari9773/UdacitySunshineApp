package in.udacity.learning.shunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.udacity.learning.shunshine.app.fragment.DetailFragment;

public class DetailActivity extends AppCompatActivity {
    private DetailFragment mDetailFragment;
   // private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState) {
//        mToolbar = (Toolbar) findViewById(R.id.tb_main);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent in = getIntent();
        String weatherRep = "";
        if (in != null) {
            weatherRep = in.getDataString();
        }

        if (savedInstanceState == null) {
            Bundle b = new Bundle();
            b.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            mDetailFragment = new DetailFragment();
            mDetailFragment.setArguments(b);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, mDetailFragment)
                    .commit();
        }
    }

}
