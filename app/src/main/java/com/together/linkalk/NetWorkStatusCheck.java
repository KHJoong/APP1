package com.together.linkalk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by kimhj on 2017-07-24.
 */

public class NetWorkStatusCheck {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(null != activeNetwork){
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                return TYPE_WIFI;
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                return TYPE_MOBILE;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context){
        int conn = NetWorkStatusCheck.getConnectivityStatus(context);
        String status = null;
        if(conn == NetWorkStatusCheck.TYPE_WIFI){
            status = "WIFI";
        } else if(conn == NetWorkStatusCheck.TYPE_MOBILE){
            status = "MOBILE";
        } else if(conn == NetWorkStatusCheck.TYPE_NOT_CONNECTED){
            status = "NOT";
        }
        return status;
    }
}
