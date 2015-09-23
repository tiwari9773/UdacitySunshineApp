package in.udacity.learning.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by USER on 03-Sep-15.
 */
public class NetWorkInfoUtility {

    private boolean isNetworkAvailable  = false;

    public boolean isWifiEnable() {
        return isWifiEnable;
    }

    public void setIsWifiEnable(boolean isWifiEnable) {
        this.isWifiEnable = isWifiEnable;
    }

    public boolean isMobileNetworkAvailable() {
        return isMobileNetworkAvailable;
    }

    public void setIsMobileNetworkAvailable(boolean isMobileNetworkAvailable) {
        this.isMobileNetworkAvailable = isMobileNetworkAvailable;
    }

    private boolean isWifiEnable = false;
    private boolean isMobileNetworkAvailable =false;

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public void setIsNetworkAvailable(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    public boolean isNetWorkAvailableNow(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        setIsWifiEnable(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());
        setIsMobileNetworkAvailable(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected());

        if(isWifiEnable() || isMobileNetworkAvailable())
        {
            setIsNetworkAvailable(true);
        }

        return  isNetworkAvailable();
    }
}
