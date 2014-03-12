package com.proper.TestDeux;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Lebel on 14/02/14.
 */
public class TestDeuxWifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //determine if wifi is on
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled()) {
            //do

        }
        //get connected configuration, access current RSSI
        //when RSSI < -60, drop current BSSID and connect to a new BSSID with stronger RSSI









        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
        boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

        NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

        if(currentNetworkInfo.isConnected()){

        }else{
            //showNotification();
        }
    }
}
