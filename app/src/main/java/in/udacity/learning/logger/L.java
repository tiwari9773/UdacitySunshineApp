package in.udacity.learning.logger;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by USER on 03-Sep-15.
 */
public class L {

    public static void lToast(Context c,String msg)
    {
        Toast.makeText(c,msg,Toast.LENGTH_SHORT).show();
    }

    public static void LToast(Context c,String msg)
    {
        Toast.makeText(c,msg,Toast.LENGTH_LONG).show();
    }

    public static void lToastTest(Context c)
    {
        Toast.makeText(c,"Test",Toast.LENGTH_SHORT).show();
    }

}
