package in.udacity.learning.shunshine.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import in.udacity.learning.constantsutility.WebServiceURL;
import in.udacity.learning.logger.L;
import in.udacity.learning.network.NetWorkInfoUtility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize(savedInstanceState);
    }

    //
    public void initialize(Bundle savedInstanceState) {

        if(savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().add(R.id.frag_container,new ForcastFragment()).commit();
        }
    }

}
