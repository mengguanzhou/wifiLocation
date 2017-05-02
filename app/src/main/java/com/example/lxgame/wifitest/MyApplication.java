package com.example.lxgame.wifitest;

import android.app.Application;
import android.content.Context;

/**
 * Created by john on 2017/4/8.
 */

public class MyApplication extends Application{

    private static APDBManager apdbManager;

    @Override
    public void onCreate() {
        super.onCreate();
        KMeansAlgorithm.init(getApplicationContext());
        DisicionTree.init(getApplicationContext());
    }

    public static void instatiateAPDBM(Context context) {
        apdbManager = new APDBManager(context);
    }

    public static APDBManager getApdbManager() {
        return apdbManager;
    }
}
