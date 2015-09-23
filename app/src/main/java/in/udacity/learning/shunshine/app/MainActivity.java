package in.udacity.learning.shunshine.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import in.udacity.learning.fragment.ForcastFragment;
import in.udacity.learning.logger.L;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize(savedInstanceState);
    }

    //
    public void initialize(Bundle savedInstanceState) {

        //setup toolbar to have more control over Actionbar /** Kindly guide on this
        // is this good approach
        // 1. When to use inbuild Actionbar?
        // 2. When to use toolbar ?*/
        mToolbar = (Toolbar)findViewById(R.id.tb_main);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

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
