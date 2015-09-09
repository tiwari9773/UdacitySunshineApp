package in.udacity.learning.shunshine.app;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import in.udacity.learning.constantsutility.AppConstant;

public class DetailActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;
    private Toolbar mToolbar;
    private String weatherRep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initialize(savedInstanceState);
    }

    private void initialize(Bundle savedInstanceState)
    {
        mToolbar = (Toolbar)findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        weatherRep = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (savedInstanceState == null) {
            Bundle b =new Bundle();
            b.putString(Intent.EXTRA_TEXT, weatherRep);
            PlaceholderFragment placeholderFragment = new PlaceholderFragment();
            placeholderFragment.setArguments(b);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
            .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.action_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent in = new Intent(this,SettingsActivity.class);
            startActivity(in);
            return true;
        }else if(id == R.id.action_item_share){
            Intent in = new Intent();
            in.setAction(Intent.ACTION_SEND);
            in.putExtra(Intent.EXTRA_TEXT, weatherRep);
            in.setType(AppConstant.TYPE_TEXT_DATA_TRANS);
            setShareIntent(in);
        }

        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.title_activity_detail)));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            TextView tvItem = (TextView)rootView.findViewById(R.id.tv_item);

            tvItem.setText( getArguments().getString(Intent.EXTRA_TEXT));
            return rootView;
        }
    }
}
