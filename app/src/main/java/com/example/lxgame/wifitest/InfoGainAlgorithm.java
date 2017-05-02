package com.example.lxgame.wifitest;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by john on 2017/3/30.
 */

public class InfoGainAlgorithm {

    private APDBManager mApdbManager;
    private Context mContext;

    public InfoGainAlgorithm(Context context) {
        mContext = context;
//        mApdbManager = new APDBManager(context);
//        for (int i = 1; i <= 102; i++) {
//            MyDBManager myDBManager = new MyDBManager(context, i);
//            myDBManagers.add(myDBManager);
//        }
    }

    public void calculateInfoGainValue(AccessPoint ap, APDBManager apdbManager) {
            if (mApdbManager == null) {
                mApdbManager = apdbManager;
            }
            int[] wholeValue = new int[102];
            for (int i = 1; i <= 102; i++) {
                MyDBManager myDBManager = new MyDBManager(mContext, i);
                int valueTemp = myDBManager.queryRssiByMac(ap.getMac());
                wholeValue[i - 1] = valueTemp;
                myDBManager.closeDB();
            }
            Arrays.sort(wholeValue);
            double entrypyCondition = 0;
            double entrypyGrid = 6.67;
            double rank = 0;
            for (int i = 0; i < wholeValue.length; i++) {
                if (wholeValue[i] != 0) {
                    rank++;
                }
            }
            for (int i = 0; i < wholeValue.length; i++) {
                double constant = 1;
                int value = wholeValue[i];
//                if (wholeValue[i] == 0) continue;
                for (int j = i + 1; j < wholeValue.length; j++) {
                    if (wholeValue[j] == value) {
                        constant++;
                        i++;
                    } else {
                        break;
                    }
                }
                if (constant == 1) {
                    continue;
                } else {
                    double reverse = 1.0 / constant;
                    entrypyCondition += (-1) * 1.0 / rank * (Math.log(reverse) / Math.log(2));
//                    entrypyCondition += (-1) * constant / rank * (Math.log(reverse) / Math.log(2));
                }
            }
            double infoGain = entrypyGrid - entrypyCondition;
            ap.setInfoGain(infoGain);
            Log.i("infogain", ap.getMac() + " " + ap.getInfoGain());
            mApdbManager.add(ap);
//            MyApplication.getApdbManager().add(ap);
    }

    public void infoGainForCluster(AccessPoint ap, int[] gridIndex, int cluster){
            int[] wholeValue = new int[gridIndex.length];
            for (int i = 0; i < gridIndex.length; i++) {
                MyDBManager myDBManager = new MyDBManager(mContext, gridIndex[i]);
                int valueTemp = myDBManager.queryRssiByMac(ap.getMac());
                wholeValue[i] = valueTemp;
                myDBManager.closeDB();
            }
            Arrays.sort(wholeValue);
            double entrypyCondition = 0;
            double entrypyGrid = 6.67;
            double rank = 0;
            for (int i = 0; i < wholeValue.length; i++) {
                if (wholeValue[i] != 0) {
                    rank++;
                }
            }
            for (int i = 0; i < wholeValue.length; i++) {
                double constant = 1;
                int value = wholeValue[i];
//                if (wholeValue[i] == 0) continue;
                for (int j = i + 1; j < wholeValue.length; j++) {
                    if (wholeValue[j] == value) {
                        constant++;
                        i++;
                    } else {
                        break;
                    }
                }
                if (constant == 1) {
                    continue;
                } else {
                    Log.i("infoGaincon", constant + "");
                    double reverse = 1.0 / constant;
                    Log.i("infoGaincon", reverse + "");
//                    entrypyCondition += (-1) * constant / gridIndex.length * (Math.log(reverse) / Math.log(2));
                    if (cluster != 0) {
                        entrypyCondition += (-1) * constant / gridIndex.length * (Math.log(reverse) / Math.log(2));
                    } else {
                        entrypyCondition += (-1) * 1.0 / gridIndex.length * (Math.log(reverse) / Math.log(2));
                    }
                    Log.i("infogainen", entrypyCondition + "");
                }
            }
        double infoGain = entrypyGrid - entrypyCondition;
        ap.setInfoGain(infoGain);
        Log.i("infogain", ap.getMac() + " " + ap.getInfoGain());
        ClusterAPDBManager clusterAPDBManageru = new ClusterAPDBManager(mContext, cluster);
        clusterAPDBManageru.add(ap);
        clusterAPDBManageru.closeDB();
    }

    public void closeDB() {
        mApdbManager.closeDB();
    }

}
