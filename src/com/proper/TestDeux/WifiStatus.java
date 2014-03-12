package com.proper.TestDeux;


import android.net.wifi.WifiInfo;

/**
 * Created by Lebel on 13/02/14.
 */
public class WifiStatus {
    private WifiInfo info;
    private int newRSSI;

    public WifiStatus(WifiInfo info) {
        this.info = info;
    }

    public WifiInfo getInfo() {
        return info;
    }

    public void setInfo(WifiInfo info) {
        this.info = info;
    }
}