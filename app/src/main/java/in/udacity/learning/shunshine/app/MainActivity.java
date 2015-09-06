package in.udacity.learning.shunshine.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import in.udacity.learning.constantsutility.WebServiceURL;
import in.udacity.learning.logger.L;
import in.udacity.learning.network.NetWorkInfoUtility;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize(savedInstanceState);
    }

    /** Commenting this because Activity is receving "refresh" event in onOptionsItemSelected() even
     * I have  used this line in fragment setHasOptionsMenu(true);
     * please explain on this */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }

    //
    public void initialize(Bundle savedInstanceState) {

        mToolbar = (Toolbar)findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);

        if(savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,new ForcastFragment()).commit();
        }
        else
        {
            L.lToast(getBaseContext(),"Test");
        }
    }

}
