package com.proper.TestDeux;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * Created by Lebel on 17/02/14.
 */
public class WifiResultSignalLevelSorter implements Comparator<ScanResult> {
    @Override
    public int compare(ScanResult lhs, ScanResult rhs) {
        int ret = 0;

        if (lhs.level  < rhs.level) {
            ret = -1;
        }else if(lhs.level > rhs.level ) {
            ret = 1;
        }else if (lhs.level == rhs.level) {
            ret = 0;
        }
        return ret;
    }
}
