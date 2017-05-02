package com.example.lxgame.wifitest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by john on 2017/4/12.
 */

class ClusterAPDBHelper extends SQLiteOpenHelper{

    private Context mContext;
    private int index;

    public ClusterAPDBHelper(Context context, int index) {
        super(context, "apinfogain" + index + ".db", null, 1);
        mContext = context;
        this.index = index;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS apInfoGain" + index + "("
                + "id integer PRIMARY KEY Autoincrement,"
                + "mac text,"
                + "infogain real,"
                + "name text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
